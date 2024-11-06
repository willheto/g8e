package com.g8e.gameserver.network;

import org.java_websocket.WebSocket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.g8e.db.CommonQueries;
import com.g8e.db.models.DBAccount;
import com.g8e.gameserver.World;
import com.g8e.db.models.DBPlayer;
import com.g8e.gameserver.models.AttackEvent;
import com.g8e.gameserver.models.Player;

import com.g8e.gameserver.network.actions.Action;
import com.g8e.gameserver.network.actions.PlayerAttackMove;
import com.g8e.gameserver.network.actions.PlayerAttackMoveData;
import com.g8e.gameserver.network.actions.PlayerMove;
import com.g8e.gameserver.network.actions.PlayerMoveData;
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

            List<AttackEvent> attackEvents = new ArrayList<>();

            GameState gameState = new GameState(attackEvents, world.getPlayers(), world.getNpcs(),
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
        Action parsedMessage = gson.fromJson(message, Action.class);
        String action = parsedMessage.getAction();
        String playerID = parsedMessage.getPlayerID();

        switch (action) {
            case "playerMove":
                PlayerMove playerMoveAction = gson.fromJson(message, PlayerMove.class);
                int x = playerMoveAction.getX();
                int y = playerMoveAction.getY();

                this.world.enqueueAction(new PlayerMove(playerID, new PlayerMoveData(x, y)));
                break;

            case "playerAttackMove":
                PlayerAttackMove playerAttackMoveAction = gson.fromJson(message, PlayerAttackMove.class);

                String entityID = playerAttackMoveAction.getEntityID();
                this.world.enqueueAction(
                        new PlayerAttackMove(playerID, new PlayerAttackMoveData(entityID)));
                break;
            /*
             * case "changeAttackStyle":
             * String attackStyle = (String) parsedMessage.getData();
             * this.world.enqueueAction(
             * new Action(conn.toString(), "changeAttackStyle", attackStyle));
             * break;
             * 
             * case "inventoryUpdate":
             * int[] inventory = (int[]) parsedMessage.getData();
             * this.world.enqueueAction(
             * new Action(conn.toString(), "inventoryUpdate", inventory));
             * break;
             * 
             * case "wieldItem":
             * int inventoryIndex = (int) parsedMessage.getData();
             * this.world.enqueueAction(
             * new Action(conn.toString(), "wieldItem", inventoryIndex));
             * break;
             * 
             * case "unwieldItem":
             * int combatEquipmentIndex = (int) parsedMessage.getData();
             * this.world.enqueueAction(
             * new Action(conn.toString(), "unwildItem", combatEquipmentIndex));
             * break;
             * 
             * case "dropItem":
             * int inventoryIndexToDrop = (int) parsedMessage.getData();
             * this.world.enqueueAction(
             * new Action(conn.toString(), "dropItem", inventoryIndexToDrop));
             * break;
             * 
             * case "questProgressUpdate":
             * QuestProgressUpdate progressUpdate = (QuestProgressUpdate)
             * parsedMessage.getData();
             * this.world.enqueueAction(
             * new Action(conn.toString(), "questProgressUpdate", progressUpdate));
             * break;
             * 
             * case "playerTakeMove":
             * String itemUniqueID = (String) parsedMessage.getData();
             * this.world.enqueueAction(
             * new Action(conn.toString(), "playerTakeMove", itemUniqueID));
             * break;
             * 
             * case "removeItemsFromInventory":
             * int[] itemIndices = (int[]) parsedMessage.getData();
             * this.world.enqueueAction(
             * new Action(conn.toString(), "removeItemsFromInventory", itemIndices));
             * break;
             * 
             * case "chatMessage":
             * // String chatMessage = (String) parsedMessage.getData();
             * // TODO: Implement chat
             * break;
             */

            default:
                break;
        }

    }

}
