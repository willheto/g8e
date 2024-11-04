package com.g8e.gameserver.models;

public class ChatMessage {
    private String playerID;
    private String timeSent;
    private String message;
    private boolean isPrivate;

    public ChatMessage(String playerID, String message, boolean isPrivate) {
        this.playerID = playerID;
        this.message = message;
        this.timeSent = java.time.LocalDateTime.now().toString();
        this.isPrivate = isPrivate;
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}
