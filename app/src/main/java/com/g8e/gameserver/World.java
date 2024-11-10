package com.g8e.gameserver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.g8e.gameserver.managers.EntitiesManager;
import com.g8e.gameserver.managers.ItemsManager;
import com.g8e.gameserver.managers.QuestsManager;
import com.g8e.gameserver.network.actions.Action;
import com.g8e.gameserver.pathfinding.AStar;
import com.g8e.gameserver.tile.TileManager;
import com.g8e.gameserver.models.AttackEvent;
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Entity;
import com.g8e.gameserver.models.EntityData;
import com.g8e.gameserver.models.Item;
import com.g8e.gameserver.models.Npc;
import com.g8e.gameserver.models.Player;
import com.g8e.gameserver.network.GameState;
import com.g8e.gameserver.network.WebSocketEventsHandler;

public class World {
    private static final int TICK_RATE = 600; // 600ms
    public final int maxWorldCol = 100;
    public final int maxWorldRow = 100;

    public WebSocketEventsHandler webSocketEventsHandler;
    public AStar pathFinder = new AStar(this);
    public TileManager tileManager = new TileManager(this);
    public ItemsManager itemsManager = new ItemsManager(this);
    public EntitiesManager entitiesManager = new EntitiesManager();
    public QuestsManager questsManager = new QuestsManager();
    public List<Player> players = new ArrayList<Player>();
    public List<Npc> npcs = new ArrayList<Npc>();
    public List<Item> items = new ArrayList<Item>();
    public List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
    public List<Action> actionQueue = new ArrayList<Action>();
    public List<AttackEvent> tickAttackEvents = new ArrayList<AttackEvent>();
    private Consumer<GameState> broadcastGameState;

    public World(Consumer<GameState> broadcastGameState) {
        this.broadcastGameState = broadcastGameState;
        this.setInitialNpcs();
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

        this.broadcastGameState
                .accept(new GameState(this.tickAttackEvents, this.players, this.npcs, this.chatMessages, this.items,
                        null));
        this.tickAttackEvents.clear();
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

    public void removePlayer(String playerID) {
        Player player = this.players.stream().filter(p -> p.entityID.equals(playerID)).findFirst().orElse(null);
        if (player != null) {
            this.players.remove(player);
        }
    }

    private void setInitialNpcs() {
        EntityData entityData = entitiesManager.getEntityDataByIndex(1);

        Npc orc = new Npc(this, 1, 50, 50, entityData.getName(), entityData.getExamine(), entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());
        this.npcs.add(orc);

        EntityData dukeData = entitiesManager.getEntityDataByIndex(2);

        Npc duke = new Npc(this, 2, 54, 50, dukeData.getName(), dukeData.getExamine(), dukeData.getRespawnTime(),
                dukeData.getSkills(), dukeData.getType());
        this.npcs.add(duke);
    }

}
