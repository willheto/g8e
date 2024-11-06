package com.g8e.gameserver.network.actions;

public class PlayerAttackMove extends Action {
    private PlayerAttackMoveData data;

    public PlayerAttackMove(String playerID, PlayerAttackMoveData data) {
        this.action = "playerAttackMove";
        this.playerID = playerID;
        this.data = data;
    }

    public String getEntityID() {
        return data.entityID;
    }

}
