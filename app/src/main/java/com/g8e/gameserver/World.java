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

import com.g8e.gameserver.managers.EntitiesManager;
import com.g8e.gameserver.managers.ItemsManager;
import com.g8e.gameserver.managers.QuestsManager;
import com.g8e.gameserver.managers.ShopsManager;
import com.g8e.gameserver.network.actions.Action;
import com.g8e.gameserver.network.compressing.Compress;
import com.g8e.gameserver.network.dataTransferModels.DTONpc;
import com.g8e.gameserver.network.dataTransferModels.DTOPlayer;
import com.g8e.gameserver.tile.TileManager;
import com.g8e.gameserver.tile.TilePosition;
import com.g8e.util.Logger;
import com.google.gson.Gson;
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Chunkable;
import com.g8e.gameserver.models.Entity;
import com.g8e.gameserver.models.EntityData;
import com.g8e.gameserver.models.Item;
import com.g8e.gameserver.models.Npc;
import com.g8e.gameserver.models.Player;
import com.g8e.gameserver.models.events.AttackEvent;
import com.g8e.gameserver.models.events.TalkEvent;
import com.g8e.gameserver.models.events.TradeEvent;
import com.g8e.gameserver.network.GameState;
import com.g8e.gameserver.network.GameStateComparator;
import com.g8e.gameserver.network.WebSocketEventsHandler;

public class World {
    private static final int TICK_RATE = 600; // 600ms
    public final int maxWorldCol = 500;
    public final int maxWorldRow = 500;
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

        EntityData entityData = entitiesManager.getEntityDataByIndex(1);

        Npc goblin = new Npc(this, 1, 82, 59, entityData.getName(),
                entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());

        Npc goblin2 = new Npc(this, 1, 78, 52, entityData.getName(),
                entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());

        Npc goblin3 = new Npc(this, 1, 84, 58, entityData.getName(),
                entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());

        Npc goblin4 = new Npc(this, 1, 80, 67, entityData.getName(),
                entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());

        Npc goblin5 = new Npc(this, 1, 73, 65, entityData.getName(),
                entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());
        this.npcs.add(goblin);
        this.npcs.add(goblin2);
        this.npcs.add(goblin3);
        this.npcs.add(goblin4);
        this.npcs.add(goblin5);

        EntityData dukeData = entitiesManager.getEntityDataByIndex(2);

        Npc duke = new Npc(this, 2, 71, 23, dukeData.getName(),
                dukeData.getExamine(), dukeData.getRespawnTime(),
                dukeData.getSkills(), dukeData.getType());
        this.npcs.add(duke);

        EntityData pasanen = entitiesManager.getEntityDataByIndex(14);
        Npc pasanenNpc = new Npc(this, 14, 71, 23, pasanen.getName(),
                pasanen.getExamine(), pasanen.getRespawnTime(),
                pasanen.getSkills(), pasanen.getType());
        this.npcs.add(pasanenNpc);

        // Stonehaven men
        TilePosition[] positions = { new TilePosition(77, 25), new TilePosition(64,
                33), new TilePosition(77, 31),
        };
        EntityData manData1 = entitiesManager.getEntityDataByIndex(3);
        EntityData manData2 = entitiesManager.getEntityDataByIndex(4);
        EntityData manData3 = entitiesManager.getEntityDataByIndex(5);

        Npc man1 = new Npc(this, 3, positions[0].x, positions[0].y,
                manData1.getName(), manData1.getExamine(),
                manData1.getRespawnTime(), manData1.getSkills(), manData1.getType());

        Npc man2 = new Npc(this, 4, positions[1].x, positions[1].y,
                manData2.getName(), manData2.getExamine(),
                manData2.getRespawnTime(), manData2.getSkills(), manData2.getType());

        Npc man3 = new Npc(this, 5, positions[2].x, positions[2].y,
                manData3.getName(), manData3.getExamine(),
                manData3.getRespawnTime(), manData3.getSkills(), manData3.getType());

        this.npcs.add(man1);
        this.npcs.add(man2);
        this.npcs.add(man3);

        EntityData priestData = entitiesManager.getEntityDataByIndex(6);

        Npc priest = new Npc(this, 6, 81, 37, priestData.getName(),
                priestData.getExamine(),
                priestData.getRespawnTime(),
                priestData.getSkills(), priestData.getType());
        this.npcs.add(priest);

        EntityData guardData = entitiesManager.getEntityDataByIndex(7);
        Npc guard = new Npc(this, 7, 43, 32, guardData.getName(),
                guardData.getExamine(), guardData.getRespawnTime(),
                guardData.getSkills(), guardData.getType());

        this.npcs.add(guard);

        EntityData bat = entitiesManager.getEntityDataByIndex(8);
        Npc bat1 = new Npc(this, 8, 56, 56, bat.getName(), bat.getExamine(),
                bat.getRespawnTime(), bat.getSkills(),
                bat.getType());

        this.npcs.add(bat1);

        EntityData chicken = entitiesManager.getEntityDataByIndex(10);
        Npc chicken1 = new Npc(this, 10, 79, 19, chicken.getName(),
                chicken.getExamine(), chicken.getRespawnTime(),
                chicken.getSkills(), chicken.getType());

        Npc chicken2 = new Npc(this, 10, 84, 20, chicken.getName(),
                chicken.getExamine(), chicken.getRespawnTime(),
                chicken.getSkills(), chicken.getType());

        Npc chicken3 = new Npc(this, 10, 82, 13, chicken.getName(),
                chicken.getExamine(), chicken.getRespawnTime(),
                chicken.getSkills(), chicken.getType());

        Npc chicken4 = new Npc(this, 10, 87, 15, chicken.getName(),
                chicken.getExamine(), chicken.getRespawnTime(),
                chicken.getSkills(), chicken.getType());

        this.npcs.add(chicken1);

        this.npcs.add(chicken2);

        this.npcs.add(chicken3);

        this.npcs.add(chicken4);

        EntityData acolyte = entitiesManager.getEntityDataByIndex(11);
        Npc acolyte1 = new Npc(this, 11, 50, 60, acolyte.getName(),
                acolyte.getExamine(), acolyte.getRespawnTime(),
                acolyte.getSkills(), acolyte.getType());
        this.npcs.add(acolyte1);
        EntityData bandit1 = entitiesManager.getEntityDataByIndex(12);
        Npc bandit = new Npc(this, 12, 91, 79, bandit1.getName(),
                bandit1.getExamine(), bandit1.getRespawnTime(),
                bandit1.getSkills(), bandit1.getType());
        this.npcs.add(bandit);
        Npc bandit2 = new Npc(this, 12, 92, 79, bandit1.getName(),
                bandit1.getExamine(), bandit1.getRespawnTime(),
                bandit1.getSkills(), bandit1.getType());
        this.npcs.add(bandit2);

        Npc bandit3Npc = new Npc(this, 13, 93, 79, bandit1.getName(),
                bandit1.getExamine(), bandit1.getRespawnTime(),
                bandit1.getSkills(), bandit1.getType());
        this.npcs.add(bandit3Npc);

        EntityData banditChief = entitiesManager.getEntityDataByIndex(13);
        Npc banditChiefNpc = new Npc(this, 13, 94, 79, banditChief.getName(),
                banditChief.getExamine(), banditChief.getRespawnTime(),
                banditChief.getSkills(), banditChief.getType());
        this.npcs.add(banditChiefNpc);

        EntityData blacksmithPasanen = entitiesManager.getEntityDataByIndex(17);

        Npc blacksmithPasanenNpc = new Npc(this, 17, 121, 81, blacksmithPasanen.getName(),
                blacksmithPasanen.getExamine(), blacksmithPasanen.getRespawnTime(),
                blacksmithPasanen.getSkills(), blacksmithPasanen.getType());

        this.npcs.add(blacksmithPasanenNpc);

        for (int i = 0; i < 4; i++) {
            EntityData entityData1 = entitiesManager.getEntityDataByIndex(19);
            Npc npc = new Npc(this, 18, 124, 85, entityData1.getName(),
                    entityData1.getExamine(), entityData1.getRespawnTime(),
                    entityData1.getSkills(), entityData1.getType());
            this.npcs.add(npc);
        }

        EntityData pasanenNpcData = entitiesManager.getEntityDataByIndex(20);

        Npc normalPasanen = new Npc(this, 20, 136, 83, pasanenNpcData.getName(),
                pasanenNpcData.getExamine(), pasanenNpcData.getRespawnTime(),
                pasanenNpcData.getSkills(), pasanenNpcData.getType());

        this.npcs.add(normalPasanen);

        EntityData bartenderPasanen = entitiesManager.getEntityDataByIndex(15);

        Npc bartenderPasanenNpc = new Npc(this, 15, 120, 93, bartenderPasanen.getName(),
                bartenderPasanen.getExamine(), bartenderPasanen.getRespawnTime(),
                bartenderPasanen.getSkills(), bartenderPasanen.getType());

        this.npcs.add(bartenderPasanenNpc);

        EntityData mayorPasanen = entitiesManager.getEntityDataByIndex(16);

        Npc mayorPasanenNpc = new Npc(this, 16, 125, 85, mayorPasanen.getName(),
                mayorPasanen.getExamine(), mayorPasanen.getRespawnTime(),
                mayorPasanen.getSkills(), mayorPasanen.getType());

        this.npcs.add(mayorPasanenNpc);
    }

    private void setInitialItems() {
        this.itemsManager.spawnItem(50, 50, 64);
    }

}
