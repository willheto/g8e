package com.g8e.db.models;

public class DBPlayer {
    private int player_id;
    private int account_id;
    private int world_x;
    private int world_y;
    private Integer weapon = null;
    private int[] inventory;
    private int[] quest_progress;
    private int attack_experience;
    private int strength_experience;
    private int defence_experience;
    private int hitpoints_experience;
    private int skin_color;
    private int hair_color;
    private int shirt_color;

    public DBPlayer(int player_id, int account_id, int skin_color, int hair_color, int shirt_color, int world_x,
            int world_y, Integer weapon, int[] inventory,
            int[] quest_progress,
            int attack_experience, int strength_experience, int defence_experience, int hitpoints_experience) {
        this.player_id = player_id;
        this.account_id = account_id;
        this.world_x = world_x;
        this.world_y = world_y;
        this.weapon = weapon;
        this.inventory = inventory;
        this.quest_progress = quest_progress;
        this.attack_experience = attack_experience;
        this.defence_experience = defence_experience;
        this.strength_experience = strength_experience;
        this.hitpoints_experience = hitpoints_experience;
        this.skin_color = skin_color;
        this.hair_color = hair_color;
        this.shirt_color = shirt_color;
    }

    public int getSkinColor() {
        return skin_color;
    }

    public int getHairColor() {
        return hair_color;
    }

    public int getShirtColor() {
        return shirt_color;
    }

    public int getPlayerID() {
        return player_id;
    }

    public int getAccountID() {
        return account_id;
    }

    public int getWorldX() {
        return world_x;
    }

    public int getWorldY() {
        return world_y;
    }

    public Integer getWeapon() {
        return weapon;
    }

    public int[] getInventory() {
        return inventory;
    }

    public int[] getQuestProgress() {
        return quest_progress;
    }

    public int getAttackExperience() {
        return attack_experience;
    }

    public int getDefenceExperience() {
        return defence_experience;
    }

    public int getStrengthExperience() {
        return strength_experience;
    }

    public int getHitpointsExperience() {
        return hitpoints_experience;
    }

}
