package com.g8e.gameserver.util;

public class CombatUtils {
    public static int getAttackHitChance(int attackValue, int defenceValue) {
        int hitChance = 50 + (attackValue - defenceValue) * 5;
        if (hitChance < 5) {
            hitChance = 5;
        } else if (hitChance > 95) {
            hitChance = 95;
        }
        return hitChance;
    }

    // TODO FIX LOGIC
    public static int getAttackDamage(int strengthExperience, int attackValue) {
        int strengthLevel = ExperienceUtils.getLevelByExp(strengthExperience);

        int damage;
        if (strengthLevel > 10) {
            damage = 1 + (strengthLevel / 10) * (attackValue / 10);
        } else {
            damage = 1 + (strengthLevel / 4) * (attackValue / 10);
        }

        return damage;
    }
}
