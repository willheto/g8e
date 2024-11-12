package com.g8e.gameserver;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.g8e.gameserver.network.GameState;
import com.g8e.gameserver.network.WebSocketEventsHandler;
import com.g8e.util.Logger;
import com.google.gson.Gson;

import io.github.cdimascio.dotenv.Dotenv;

public class GameServer extends WebSocketServer {
    static Dotenv dotenv = Dotenv.load();

    private final WebSocketEventsHandler eventsHandler;
    private final World world = new World(gameState -> broadcastGameState(gameState));

    public GameServer() {
        super(new InetSocketAddress(Integer.parseInt(dotenv.get("GAME_SERVER_PORT"))));
        this.eventsHandler = new WebSocketEventsHandler(world);
    }

    public void startServer() {
        try {
            start();
            world.start();
            handleConsoleInput();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleConsoleInput() throws IOException, InterruptedException {
        try (BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            while ((input = sysin.readLine()) != null && !input.equals("exit")) {
                broadcast(input);
            }
        }

    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Logger.printDebug("New connection from " + conn.getRemoteSocketAddress());
        Map<String, String> queryParams = getQueryParams(handshake.getResourceDescriptor());
        eventsHandler.handleConnection(conn, queryParams);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        eventsHandler.handleMessage(conn, message);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Logger.printInfo(conn + " has disconnected");
        world.removePlayer(conn.toString());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
    }

    @Override
    public void onStart() {
        Logger.printInfo("Game server started on port: " + getPort());

    }

    public void broadcastGameState(GameState gameState) {

        String gameStateJson = new Gson().toJson(gameState);
        byte[] compressedData = compress(gameStateJson);
        for (WebSocket conn : getConnections()) {
            conn.send(compressedData);
        }
    }

    public byte[] compress(String data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_COMPRESSION))) {
            dos.write(data.getBytes());
            dos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, String> getQueryParams(String resourceDescriptor) {
        Map<String, String> queryParams = new HashMap<>();
        if (resourceDescriptor.contains("?")) {
            String[] parts = resourceDescriptor.split("\\?");
            if (parts.length > 1) {
                String[] pairs = parts[1].split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        queryParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }
        return queryParams;
    }

}
