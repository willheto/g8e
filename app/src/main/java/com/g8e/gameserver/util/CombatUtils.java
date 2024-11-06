package com.g8e.gameserver.util;

public class CombatUtils {
    public static int calculateHitChance(int attackLevel, int defenseLevel, int accuracyBonus, int defenseBonus) {
        // Calculate an effective attack and defense score based on levels and bonuses
        double effectiveAttack = attackLevel * (1 + accuracyBonus / 100.0);
        double effectiveDefense = defenseLevel * (1 + defenseBonus / 100.0);

        // Calculate base hit chance as a ratio of effective attack over total of both
        double hitChance = (effectiveAttack / (effectiveAttack + effectiveDefense)) * 100;

        // Constrain hit chance between 5% and 95%
        if (hitChance < 5) {
            hitChance = 5;
        } else if (hitChance > 95) {
            hitChance = 95;
        }

        return (int) hitChance;
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
