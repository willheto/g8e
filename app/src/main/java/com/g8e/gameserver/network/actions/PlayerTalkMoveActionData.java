package com.g8e.gameserver.network.actions;

public class PlayerTalkMoveActionData {

    private String entityID;

    public PlayerTalkMoveActionData(String entityID) {
        this.entityID = entityID;
    }

    public String getEntityID() {
        return entityID;
    }

}
