package com.g8e.gameserver.models;

public class DropTable {
    private int itemID;
    private float dropChance;

    public DropTable(int itemID, float dropChance) {
        this.itemID = itemID;
        this.dropChance = dropChance;
    }

    public int getItemID() {
        return itemID;
    }

    public float getDropChance() {
        return dropChance;
    }

}
