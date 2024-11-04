package com.g8e.gameserver.models;

public class Action {
    private String playerID;
    private String action;
    private Object data;

    public Action(String playerID, String action, Object data) {
        this.playerID = playerID;
        this.action = action;
        this.data = data;
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }

}
