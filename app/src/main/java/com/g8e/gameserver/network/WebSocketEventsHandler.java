package com.g8e.gameserver.network;

import org.java_websocket.WebSocket;
import java.sql.SQLException;
import java.util.Map;

import com.g8e.db.CommonQueries;
import com.g8e.db.models.DBAccount;
import com.g8e.gameserver.World;
import com.g8e.db.models.DBPlayer;
import com.g8e.gameserver.models.Action;
import com.g8e.gameserver.models.Player;
import com.g8e.gameserver.models.TilePosition;
import com.g8e.util.Logger;
import com.google.gson.Gson;

public class WebSocketEventsHandler {
    private final World world;

    public WebSocketEventsHandler(World world) {
        this.world = world;

    }

    public void handleConnection(WebSocket conn, Map<String, String> queryParams) {
        String loginToken = queryParams.get("loginToken");
        if (loginToken == null) {
            System.out.println("Player connected without login token");
            conn.close();
            return;
        }

        DBAccount account;
        try {
            account = CommonQueries.getAccountByLoginToken(loginToken);
            if (account == null) {
                System.out.println("Player connected with invalid login token");
                conn.close();
                return;
            }

            DBPlayer player;

            player = CommonQueries.getPlayerByAccountId(account.getAccountId());

            if (player == null) {
                System.out.println("Player not found");
                conn.close();
                return;
            }

            String uniquePlayerID = conn.toString();

            Player playerToBeAdded = new Player(this.world, player, uniquePlayerID, account.getUsername(),
                    account.getAccountId());

            world.addPlayer(playerToBeAdded);

            GameState gameState = new GameState(world.getTickUpdateTime(), world.getPlayers(), world.getNpcs(),
                    conn.toString());

            Logger.printDebug(new Gson().toJson(gameState));
            conn.send(new Gson().toJson(gameState));

        } catch (SQLException e) {
            Logger.printError(loginToken + " failed to connect to the game server");
            e.printStackTrace();
        }
    }

    public void handleMessage(WebSocket conn, String message) {

        Gson gson = new Gson();
        SocketAction parsedMessage = gson.fromJson(message, SocketAction.class);

        String action = parsedMessage.getAction();

        switch (action) {
            case "playerMove":
                TilePosition target = (TilePosition) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "playerMove", target));
                break;

            case "playerAttackMove":
                String entityID = (String) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "playerAttackMove", entityID));
                break;

            case "changeAttackStyle":
                String attackStyle = (String) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "changeAttackStyle", attackStyle));
                break;

            case "inventoryUpdate":
                int[] inventory = (int[]) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "inventoryUpdate", inventory));
                break;

            case "wieldItem":
                int inventoryIndex = (int) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "wieldItem", inventoryIndex));
                break;

            case "unwieldItem":
                int combatEquipmentIndex = (int) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "unwildItem", combatEquipmentIndex));
                break;

            case "dropItem":
                int inventoryIndexToDrop = (int) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "dropItem", inventoryIndexToDrop));
                break;

            case "questProgressUpdate":
                QuestProgressUpdate progressUpdate = (QuestProgressUpdate) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "questProgressUpdate", progressUpdate));
                break;

            case "playerTakeMove":
                String itemUniqueID = (String) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "playerTakeMove", itemUniqueID));
                break;

            case "removeItemsFromInventory":
                int[] itemIndices = (int[]) parsedMessage.getData();
                this.world.enqueueAction(
                        new Action(conn.toString(), "removeItemsFromInventory", itemIndices));
                break;

            case "chatMessage":
                // String chatMessage = (String) parsedMessage.getData();
                // TODO: Implement chat
                break;

            default:
                break;
        }

    }

}
