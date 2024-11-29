package com.g8e.gameserver.models.objects;

public class Item {
    private String uniqueID;
    private int itemID;
    private String name;
    private String examine;
    private boolean isWieldable;
    private String spriteName;
    private int value;
    private boolean isStackable;
    private int amount;
    private Integer worldX = null;
    private Integer worldY = null;

    public Item(int itemID, String name, String examine, boolean isWieldable, boolean isStackable, String spriteName,
            int Value) {
        this.itemID = itemID;
        this.name = name;
        this.examine = examine;
        this.isWieldable = isWieldable;
        this.isStackable = isStackable;
        this.spriteName = spriteName;
        this.value = Value;
        this.amount = 1;
    }

    public int getValue() {
        return value;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isStackable() {
        return isStackable;
    }

    public void setStackable(boolean stackable) {
        isStackable = stackable;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExamine() {
        return examine;
    }

    public void setExamine(String examine) {
        this.examine = examine;
    }

    public boolean isWieldable() {
        return isWieldable;
    }

    public void setWieldable(boolean wieldable) {
        isWieldable = wieldable;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public void setSpriteName(String spriteName) {
        this.spriteName = spriteName;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public Integer getWorldX() {
        return worldX;
    }

    public void setWorldX(Integer worldX) {
        this.worldX = worldX;
    }

    public Integer getWorldY() {
        return worldY;
    }

    public void setWorldY(Integer worldY) {
        this.worldY = worldY;
    }

}