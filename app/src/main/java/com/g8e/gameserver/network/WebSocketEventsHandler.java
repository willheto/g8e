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
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.network.actions.ChatMessageAction;
import com.g8e.gameserver.network.actions.DropItemAction;
import com.g8e.gameserver.network.actions.EatItemAction;
import com.g8e.gameserver.models.Player;

import com.g8e.gameserver.network.actions.Action;
import com.g8e.gameserver.network.actions.PlayerAttackMove;
import com.g8e.gameserver.network.actions.PlayerAttackMoveData;
import com.g8e.gameserver.network.actions.PlayerMove;
import com.g8e.gameserver.network.actions.PlayerMoveData;
import com.g8e.gameserver.network.actions.PlayerTakeMoveAction;
import com.g8e.gameserver.network.actions.QuestProgressUpdateAction;
import com.g8e.gameserver.network.actions.UnwieldAction;
import com.g8e.gameserver.network.actions.UseItemAction;
import com.g8e.gameserver.network.actions.WieldItemAction;
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

            ChatMessage welcomeMessage = new ChatMessage(playerToBeAdded.name, "Welcome to the game!",
                    System.currentTimeMillis(),
                    false);

            ChatMessage tutorialMessage = new ChatMessage(playerToBeAdded.name,
                    "You can interact with the world using your mouse.", System.currentTimeMillis(), false);
            ChatMessage tutorialMessage2 = new ChatMessage(playerToBeAdded.name,
                    "Open stats menu and inventory by pressing TAB.",
                    System.currentTimeMillis(), false);

            world.addChatMessage(welcomeMessage);
            world.addChatMessage(tutorialMessage);
            world.addChatMessage(tutorialMessage2);

            GameState gameState = new GameState(attackEvents, world.getPlayers(), world.getNpcs(),
                    world.getChatMessages(),
                    world.getItems(),
                    conn.toString());

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

            case "chatMessage":
                ChatMessageAction chatMessage = gson.fromJson(message, ChatMessageAction.class);
                Player player = this.world.players.stream().filter(p -> p.entityID.equals(playerID)).findFirst().get();
                String senderName = player != null ? player.name : "";
                ChatMessage chatMessageModel = new ChatMessage(senderName, chatMessage.getMessage(),
                        chatMessage.getTimeSent(), chatMessage.isGlobal());
                this.world.addChatMessage(chatMessageModel);
                break;

            case "dropItem":
                DropItemAction dropItemAction = gson.fromJson(message, DropItemAction.class);
                this.world.enqueueAction(dropItemAction);
                break;

            case "wieldItem":
                WieldItemAction wieldItemAction = gson.fromJson(message, WieldItemAction.class);
                this.world.enqueueAction(wieldItemAction);
                break;

            case "unwieldItem":
                UnwieldAction unwieldItemAction = gson.fromJson(message, UnwieldAction.class);
                this.world.enqueueAction(unwieldItemAction);
                break;
            case "playerTakeMove":
                PlayerTakeMoveAction playerTakeMoveAction = gson.fromJson(message, PlayerTakeMoveAction.class);
                this.world.enqueueAction(playerTakeMoveAction);
                break;

            case "useItem":
                UseItemAction useItemAction = gson.fromJson(message, UseItemAction.class);
                this.world.enqueueAction(useItemAction);
                break;

            case "eatItem":
                EatItemAction eatItemAction = gson.fromJson(message, EatItemAction.class);
                this.world.enqueueAction(eatItemAction);
                break;
            case "questProgressUpdate":
                QuestProgressUpdateAction questProgressUpdateAction = gson.fromJson(message,
                        QuestProgressUpdateAction.class);
                this.world.enqueueAction(questProgressUpdateAction);
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
