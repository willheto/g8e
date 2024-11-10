package com.g8e.gameserver.network.actions;

public class PlayerTakeMoveActionData {
    private String uniqueItemID;

    public PlayerTakeMoveActionData(String uniqueItemID) {
        this.uniqueItemID = uniqueItemID;
    }

    public String getUniqueItemID() {
        return uniqueItemID;
    }

}
