package com.g8e.gameserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.g8e.gameserver.managers.ItemsManager;
import com.g8e.gameserver.managers.QuestsManager;
import com.g8e.gameserver.managers.TileManager;
import com.g8e.gameserver.models.Action;
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Entity;
import com.g8e.gameserver.models.Item;
import com.g8e.gameserver.models.Npc;
import com.g8e.gameserver.models.Player;
import com.g8e.gameserver.network.GameState;
import com.g8e.gameserver.network.WebSocketEventsHandler;
import com.g8e.gameserver.pathfinding.Pathfinding;
import com.g8e.util.Logger;

public class World {
    WebSocketEventsHandler webSocketEventsHandler;
    public final int maxWorldCol = 100;
    public final int maxWorldRow = 100;
    public Pathfinding pathfinding;

    private static final int TICK_RATE = 600; // 600ms
    public TileManager tileManager = new TileManager(this);
    public ItemsManager itemsManager = new ItemsManager(this);
    public QuestsManager questsManager = new QuestsManager();
    public List<Player> players = new ArrayList<Player>();
    public List<Npc> npcs = new ArrayList<Npc>();
    public List<Item> items = new ArrayList<Item>();
    public List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
    public int[][] worldMap;
    List<Action> actionQueue = new ArrayList<Action>();
    int tickUpdateTime = 0;

    private Consumer<GameState> broadcastGameState;

    public World(Consumer<GameState> broadcastGameState) {
        this.broadcastGameState = broadcastGameState;
    }

    public void start() {
        while (true) {
            try {
                Thread.sleep(TICK_RATE);
                Logger.printDebug("Game tick");
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
        });

        this.npcs.forEach(npc -> {
            npc.update();
        });

        this.tickUpdateTime = TICK_RATE;
        this.broadcastGameState.accept(new GameState(this.tickUpdateTime, this.players, this.npcs, null));

    }

    public int getTickUpdateTime() {
        return tickUpdateTime;
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
            if (player != null && player.entityID == entityID) {
                return player;
            }
        }

        for (Npc npc : npcs) {
            if (npc != null && npc.entityID == entityID) {
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

    public void loadMap(String filePath) {
        int mapTileNum[][] = new int[maxWorldCol][maxWorldRow];

        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (col < this.maxWorldCol && row < this.maxWorldRow) {
                String line = br.readLine();

                while (col < this.maxWorldCol) {
                    String numbers[] = line.split(",");

                    int num = Integer.parseInt(numbers[col]);

                    mapTileNum[col][row] = num;
                    col++;
                }
                if (col == this.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();

            this.worldMap = mapTileNum;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removePlayer(String playerID) {
        Player player = this.players.stream().filter(p -> p.entityID.equals(playerID)).findFirst().orElse(null);
        if (player != null) {
            this.players.remove(player);
        }
    }

}
