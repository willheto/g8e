package com.g8e.gameserver.network.actions;

public class RemoveItemFromInventoryAction extends Action {

    private int itemID;

    public RemoveItemFromInventoryAction(String playerID, int itemID) {
        this.action = "removeItemFromInventory";
        this.playerID = playerID;
        this.itemID = itemID;
    }

    public int getItemID() {
        return itemID;
    }

}
