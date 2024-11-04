package com.g8e.gameserver.network;

public class SocketAction {
    private String action;
    private Object data;

    public SocketAction(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }
}
