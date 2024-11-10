package com.g8e.gameserver.models;

public class Wieldable {
    int itemID;
    int accuracyBonus;
    int attackBonus;
    int defenceBonus;
    int attackSpeed;

    public Wieldable(int itemID, int accuracyBonus, int attackBonus, int defenceBonus, int attackSpeed) {
        this.itemID = itemID;
        this.accuracyBonus = accuracyBonus;
        this.attackBonus = attackBonus;
        this.defenceBonus = defenceBonus;
        this.attackSpeed = attackSpeed;
    }

    public int getItemID() {
        return itemID;
    }

    public int getAccuracyBonus() {
        return accuracyBonus;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getDefenceBonus() {
        return defenceBonus;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }
}
