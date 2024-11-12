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
import com.g8e.gameserver.tile.TilePosition;
import com.g8e.gameserver.models.AttackEvent;
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Entity;
import com.g8e.gameserver.models.EntityData;
import com.g8e.gameserver.models.Item;
import com.g8e.gameserver.models.Npc;
import com.g8e.gameserver.models.Player;
import com.g8e.gameserver.models.TalkEvent;
import com.g8e.gameserver.network.GameState;
import com.g8e.gameserver.network.WebSocketEventsHandler;

public class World {
    private static final int TICK_RATE = 600; // 600ms
    private static final TilePosition EntityData = null;
    public final int maxWorldCol = 100;
    public final int maxWorldRow = 100;

    public WebSocketEventsHandler webSocketEventsHandler;
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
    public List<TalkEvent> tickTalkEvents = new ArrayList<TalkEvent>();
    private Consumer<GameState> broadcastGameState;

    public World(Consumer<GameState> broadcastGameState) {
        this.broadcastGameState = broadcastGameState;
        this.setInitialNpcs();
        this.setInitialItems();
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
                .accept(new GameState(this.tickAttackEvents, this.tickTalkEvents, this.players, this.npcs,
                        this.chatMessages, this.items,
                        null));
        this.tickAttackEvents.clear();
        this.tickTalkEvents.clear();
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

        Npc goblin = new Npc(this, 1, 82, 59, entityData.getName(), entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());

        Npc goblin2 = new Npc(this, 1, 78, 52, entityData.getName(), entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());

        Npc goblin3 = new Npc(this, 1, 84, 58, entityData.getName(), entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());

        Npc goblin4 = new Npc(this, 1, 80, 67, entityData.getName(), entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());

        Npc goblin5 = new Npc(this, 1, 73, 65, entityData.getName(), entityData.getExamine(),
                entityData.getRespawnTime(),
                entityData.getSkills(), entityData.getType());
        this.npcs.add(goblin);
        this.npcs.add(goblin2);
        this.npcs.add(goblin3);
        this.npcs.add(goblin4);
        this.npcs.add(goblin5);

        EntityData dukeData = entitiesManager.getEntityDataByIndex(2);

        Npc duke = new Npc(this, 2, 71, 23, dukeData.getName(), dukeData.getExamine(), dukeData.getRespawnTime(),
                dukeData.getSkills(), dukeData.getType());
        this.npcs.add(duke);

        // Stonehaven men
        TilePosition[] positions = { new TilePosition(77, 25), new TilePosition(64, 33), new TilePosition(77, 31),
        };
        EntityData manData1 = entitiesManager.getEntityDataByIndex(3);
        EntityData manData2 = entitiesManager.getEntityDataByIndex(4);
        EntityData manData3 = entitiesManager.getEntityDataByIndex(5);

        Npc man1 = new Npc(this, 3, positions[0].x, positions[0].y, manData1.getName(), manData1.getExamine(),
                manData1.getRespawnTime(), manData1.getSkills(), manData1.getType());

        Npc man2 = new Npc(this, 4, positions[1].x, positions[1].y, manData2.getName(), manData2.getExamine(),
                manData2.getRespawnTime(), manData2.getSkills(), manData2.getType());

        Npc man3 = new Npc(this, 5, positions[2].x, positions[2].y, manData3.getName(), manData3.getExamine(),
                manData3.getRespawnTime(), manData3.getSkills(), manData3.getType());

        this.npcs.add(man1);
        this.npcs.add(man2);
        this.npcs.add(man3);

        EntityData priestData = entitiesManager.getEntityDataByIndex(6);

        Npc priest = new Npc(this, 6, 81, 37, priestData.getName(), priestData.getExamine(),
                priestData.getRespawnTime(),
                priestData.getSkills(), priestData.getType());
        this.npcs.add(priest);

        EntityData guardData = entitiesManager.getEntityDataByIndex(7);
        Npc guard = new Npc(this, 7, 43, 32, guardData.getName(), guardData.getExamine(), guardData.getRespawnTime(),
                guardData.getSkills(), guardData.getType());

        this.npcs.add(guard);

        EntityData bat = entitiesManager.getEntityDataByIndex(8);
        Npc bat1 = new Npc(this, 8, 56, 56, bat.getName(), bat.getExamine(), bat.getRespawnTime(), bat.getSkills(),
                bat.getType());

        this.npcs.add(bat1);

    }

    private void setInitialItems() {
        this.itemsManager.spawnItem(50, 50, 64);
    }

}
