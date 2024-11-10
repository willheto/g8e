package com.g8e.gameserver.network.actions;

public class UnwieldActionData {
    private int inventoryIndex;

    public UnwieldActionData(int inventoryIndex) {
        this.inventoryIndex = inventoryIndex;
    }

    public int getInventoryIndex() {
        return inventoryIndex;
    }

}
