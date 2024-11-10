package com.g8e.gameserver.network.actions;

public class UnwieldAction extends Action {

    private UnwieldActionData data;

    public UnwieldAction(String playerID, UnwieldActionData data) {
        this.action = "unwield";
        this.playerID = playerID;
        this.data = data;
    }

    public int getInventoryIndex() {
        return data.getInventoryIndex();
    }

}
