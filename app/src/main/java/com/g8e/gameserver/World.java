package com.g8e.gameserver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.g8e.gameserver.managers.ItemsManager;
import com.g8e.gameserver.managers.QuestsManager;
import com.g8e.gameserver.network.actions.Action;
import com.g8e.gameserver.pathfinding.AStar;
import com.g8e.gameserver.tile.TileManager;
import com.g8e.gameserver.models.AttackEvent;
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Entity;
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

        this.broadcastGameState.accept(new GameState(this.tickAttackEvents, this.players, this.npcs, null));
        this.tickAttackEvents.clear();
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
        // Orc
        int[] skills = new int[4];
        skills[0] = 10;
        skills[1] = 10;
        skills[2] = 10;
        skills[3] = 1000;

        Npc orc = new Npc(this, 54, 48, "Orc", "A nasty fella.", 20, skills, 2);
        this.npcs.add(orc);
    }

}
