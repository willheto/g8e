package com.g8e.gameserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import com.g8e.gameserver.constants.NpcConstants;
import com.g8e.gameserver.managers.EntitiesManager;
import com.g8e.gameserver.managers.ItemsManager;
import com.g8e.gameserver.managers.QuestsManager;
import com.g8e.gameserver.managers.ShopsManager;
import com.g8e.gameserver.network.actions.Action;
import com.g8e.gameserver.network.compressing.Compress;
import com.g8e.gameserver.network.dataTransferModels.DTONpc;
import com.g8e.gameserver.network.dataTransferModels.DTOPlayer;
import com.g8e.gameserver.tile.TileManager;
import com.g8e.util.Logger;
import com.google.gson.Gson;
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Chunkable;
import com.g8e.gameserver.models.entities.Entity;
import com.g8e.gameserver.models.entities.EntityData;
import com.g8e.gameserver.models.entities.Npc;
import com.g8e.gameserver.models.entities.Player;
import com.g8e.gameserver.models.events.AttackEvent;
import com.g8e.gameserver.models.events.TalkEvent;
import com.g8e.gameserver.models.events.TradeEvent;
import com.g8e.gameserver.models.objects.Item;
import com.g8e.gameserver.network.GameState;
import com.g8e.gameserver.network.GameStateComparator;
import com.g8e.gameserver.network.WebSocketEventsHandler;

public class World {
    private static final int TICK_RATE = 600; // 600ms
    public final int maxWorldCol = 300;
    public final int maxWorldRow = 300;
    public final int maxPlayers = 1000;

    public WebSocketEventsHandler webSocketEventsHandler;
    public TileManager tileManager = new TileManager(this);
    public ItemsManager itemsManager = new ItemsManager(this);
    public EntitiesManager entitiesManager = new EntitiesManager();
    public QuestsManager questsManager = new QuestsManager();
    public ShopsManager shopsManager = new ShopsManager();
    public List<Player> players = new ArrayList<Player>();
    public List<Npc> npcs = new ArrayList<Npc>();
    public List<Item> items = new ArrayList<Item>();
    public List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
    public List<Action> actionQueue = new ArrayList<Action>();
    public List<AttackEvent> tickAttackEvents = new ArrayList<AttackEvent>();
    public List<TalkEvent> tickTalkEvents = new ArrayList<TalkEvent>();
    public List<TradeEvent> tickTradeEvents = new ArrayList<TradeEvent>();

    public GameState previousGameState;
    public WebSocket[] connections = new WebSocket[maxPlayers];
    public List<String> onlinePlayers = new ArrayList<String>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> combatChecks = new ConcurrentHashMap<>();

    public World() {
        this.setInitialNpcs();
        this.setInitialItems();
    }

    public WebSocket[] getConnections() {
        return connections;
    }

    public List<String> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void addConnection(WebSocket conn) {
        for (int i = 0; i < maxPlayers; i++) {
            if (connections[i] == null) {
                connections[i] = conn;
                onlinePlayers.add(conn.toString());
                break;
            }
        }
    }

    public void removeConnection(WebSocket conn) {
        for (int i = 0; i < maxPlayers; i++) {
            if (connections[i] == conn) {
                connections[i] = null;
                onlinePlayers.remove(conn.toString());
                break;
            }
        }
    }

    public void start() {
        while (true) {
            try {
                Thread.sleep(TICK_RATE);
                gameTick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addChatMessage(ChatMessage chatMessage) {
        this.chatMessages.add(chatMessage);
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    private void gameTick() {
        this.players.forEach(player -> {

            List<Action> playerActions = this.actionQueue.stream()
                    .filter(action -> action.getPlayerID().equals(player.entityID)).toList();

            player.setTickActions(playerActions);
            player.update();

            // Remove the actions that have been processed
            this.actionQueue.removeAll(playerActions);
        });

        this.npcs.forEach(npc -> {
            npc.update();
        });

        itemsManager.updateDespawnTimers();
        sentGameStateToConnections();

    }

    private void sentGameStateToConnections() {
        GameState newGameState = getChangedGameState();

        for (WebSocket conn : connections) {
            if (conn != null) {
                Player player = this.players.stream().filter(p -> p.entityID.equals(conn.toString())).findFirst()
                        .orElse(null);

                if (player != null) {
                    int[] neighborChunks = tileManager.getNeighborChunks(player.currentChunk);

                    if (player.needsFullChunkUpdate) {
                        List<DTONpc> npcsInCurrentAndNeighborChunks = getEntitiesInCurrentAndNeighborChunks(player,
                                this.npcs, neighborChunks)
                                .stream().map(DTONpc::new).toList();

                        List<DTOPlayer> playersInCurrentAndNeighborChunks = getEntitiesInCurrentAndNeighborChunks(
                                player, this.players, neighborChunks)
                                .stream().map(DTOPlayer::new).toList();

                        newGameState.setNpcs(npcsInCurrentAndNeighborChunks);
                        newGameState.setPlayers(playersInCurrentAndNeighborChunks);
                        player.needsFullChunkUpdate = false;
                    } else {
                        List<DTONpc> npcsInCurrentAndNeighborChunks = getEntitiesInCurrentAndNeighborChunks(player,
                                newGameState.getNpcs(), neighborChunks);
                        List<DTOPlayer> playersInCurrentAndNeighborChunks = getEntitiesInCurrentAndNeighborChunks(
                                player, newGameState.getPlayers(), neighborChunks);

                        newGameState.setNpcs(npcsInCurrentAndNeighborChunks);
                        newGameState.setPlayers(playersInCurrentAndNeighborChunks);

                        // add current player if not in the list
                        if (playersInCurrentAndNeighborChunks.stream()
                                .noneMatch(p -> p.getEntityID().equals(player.entityID))) {
                            playersInCurrentAndNeighborChunks.add(new DTOPlayer(player));
                        }
                    }

                    String gameStateJson = new Gson().toJson(newGameState);
                    byte[] compressedData = Compress.compress(gameStateJson);

                    try {
                        conn.send(compressedData);
                    } catch (WebsocketNotConnectedException e) {
                        Logger.printInfo("Connection " + conn
                                + " is not connected, probably in combat and waiting to be logged out");
                    }

                }
            }
        }

        this.tickAttackEvents.clear();
        this.tickTalkEvents.clear();
        this.tickTradeEvents.clear();
    }

    private <T extends Chunkable> List<T> getEntitiesInCurrentAndNeighborChunks(Player player, List<T> entities,
            int[] neighborChunks) {
        return entities.stream()
                .filter(e -> e.getCurrentChunk() == player.currentChunk
                        || Arrays.stream(neighborChunks).anyMatch(chunk -> chunk == e.getCurrentChunk()))
                .collect(Collectors.toList());
    }

    private GameState getChangedGameState() {
        List<DTOPlayer> dtoPlayers = this.players.stream().map(player -> new DTOPlayer(player)).toList();
        List<DTONpc> dtoNpcs = this.npcs.stream().map(npc -> new DTONpc(npc)).toList();

        GameState newGameState = new GameState(this.tickAttackEvents, this.tickTalkEvents, this.tickTradeEvents,
                dtoPlayers, dtoNpcs,
                this.chatMessages,
                this.items, null, this.onlinePlayers, this.shopsManager.getShops());
        if (previousGameState == null) {
            previousGameState = newGameState;
            return newGameState;
        } else {
            GameState changedGameState = GameStateComparator.getChangedGameState(previousGameState, newGameState);
            previousGameState = newGameState;
            return changedGameState;
        }

    }

    public List<Item> getItems() {
        return items;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Npc> getNpcs() {
        return npcs;
    }

    public void enqueueAction(Action action) {
        this.actionQueue.add(action);
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public Entity getEntityByID(String entityID) {
        for (Player player : players) {
            if (entityID.equals(player.entityID)) {
                return player;
            }
        }

        for (Npc npc : npcs) {
            if (entityID.equals(npc.entityID)) {
                return npc;
            }
        }

        return null;
    }

    public Item getItemByID(String itemUniqueID) {
        Item item = null;
        for (Item i : items) {
            if (i != null && i.getUniqueID().equals(itemUniqueID)) {
                item = i;
                break;
            }
        }
        return item;
    }

    public void removePlayer(WebSocket conn) {
        String playerID = conn.toString();
        Player player = this.players.stream().filter(p -> p.entityID.equals(playerID)).findFirst().orElse(null);
        if (player != null) {
            if (player.isInCombat == false) {
                this.players.remove(player);
                removeConnection(conn);
            } else {
                // Schedule a task to check every 600 milliseconds if the player is still in
                // combat
                ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
                    if (!player.isInCombat) {
                        players.remove(player);
                        removeConnection(conn);

                        System.out.println("Player removed from game after combat ended");

                        // Cancel this specific task
                        combatChecks.get(playerID).cancel(true);
                        combatChecks.remove(playerID);
                    }
                }, 0, 600, TimeUnit.MILLISECONDS);

                // Store the ScheduledFuture in the map
                combatChecks.put(playerID, future);
            }

            // set interval to check if player is still in combat

        }
    }

    private void setInitialNpcs() {
        addNpc(NpcConstants.GOBLIN, 82, 59, 7);
        addNpc(NpcConstants.GOBLIN, 78, 52, 7);
        addNpc(NpcConstants.GOBLIN, 84, 58, 10);
        addNpc(NpcConstants.GOBLIN, 80, 67, 10);
        addNpc(NpcConstants.GOBLIN, 73, 65, 10);
        addNpc(NpcConstants.DUKE, 71, 23, 10);
        addNpc(NpcConstants.SHOPKEEPER, 69, 21, 2);

        addNpc(NpcConstants.MAN, 77, 25, 7);
        addNpc(NpcConstants.MAN2, 64, 33, 7);
        addNpc(NpcConstants.MAN3, 77, 31, 7);

        addNpc(NpcConstants.PRIEST, 81, 37, 4);
        addNpc(NpcConstants.GUARD, 43, 32, 4);
        addNpc(NpcConstants.BAT, 56, 56, 8);

        addNpc(NpcConstants.CHICKEN, 79, 19, 5);
        addNpc(NpcConstants.CHICKEN, 84, 20, 5);
        addNpc(NpcConstants.CHICKEN, 82, 13, 4);
        addNpc(NpcConstants.CHICKEN, 87, 15, 4);

        addNpc(NpcConstants.ACOLYTE, 50, 60, 3);
        addNpc(NpcConstants.BANDIT, 131, 86, 4);
        addNpc(NpcConstants.BANDIT, 133, 85, 4);
        addNpc(NpcConstants.BANDIT, 131, 84, 5);
        addNpc(NpcConstants.BANDIT_CHIEF, 132, 87, 5);

        addNpc(NpcConstants.BLACKSMITH_PASANEN, 200, 108, 2);

        for (int i = 0; i < 2; i++) {
            addNpc(NpcConstants.GUARD_PASANEN, 172, 95, 4);
        }

        addNpc(NpcConstants.PASANEN, 194, 113, 7);
        for (int i = 0; i < 4; i++) {
            addNpc(NpcConstants.PASANEN, 189, 96, 8);
        }
        addNpc(NpcConstants.BARTENDER_PASANEN, 181, 105, 3);
        addNpc(NpcConstants.MAYOR_PASANEN, 179, 88, 3);
    }

    private void addNpc(int index, int x, int y, int wanderRange) {
        EntityData entityData = entitiesManager.getEntityDataByIndex(index);
        Npc npc = new Npc(this, index, x, y, entityData.getName(),
                entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(),
                entityData.getType());
        this.npcs.add(npc);
        npc.setWanderRange(wanderRange);
    }

    private void setInitialItems() {
        this.itemsManager.spawnItem(50, 50, 64);
    }

}
