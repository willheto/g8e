package com.g8e.gameserver.network.actions;

public class PlayerTalkMoveAction extends Action {
    private PlayerTalkMoveActionData data;

    public PlayerTalkMoveAction(String playerID, PlayerTalkMoveActionData data) {
        this.action = "playerTalkMove";
        this.playerID = playerID;
        this.data = data;
    }

    public String getEntityID() {
        return data.getEntityID();
    }

}
