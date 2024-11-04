package com.g8e.gameserver.models;

import com.g8e.gameserver.World;
import com.g8e.gameserver.util.ExperienceUtils;
import com.g8e.gameserver.util.SkillUtils;

public class Npc extends Combatant {
    private int respawnTime;
    private int respawnTickCounter;
    public boolean isDead;

    public Npc(World world, int worldX, int worldY, String name, String examine, int respawnTime, int[] skills,
            int type) {
        super("npc" + (int) (Math.random() * 1000000), world, worldX, worldY, name, examine, type);

        this.respawnTime = respawnTime;
        this.skills = skills;
        this.currentHitpoints = ExperienceUtils.getLevelByExp(this.skills[SkillUtils.HITPOINTS]);
        this.weapon = 1;
        this.combatLevel = getCombatLevel();

    }

    public void update() {
        if (isDead) {
            this.resetNpc();
            if (this.respawnTickCounter < this.respawnTime) {
                this.respawnTickCounter++;
            } else {
                this.respawnTickCounter = 0;
                this.isDead = false;
            }
        }

        if (this.targetedEntityID == null || this.isTargetTileNotWithinWanderArea()) {
            this.setNewTargetTileWithingWanderArea();
        }

        this.updateCounters();

        if (this.targetedEntityID != null) {
            if (this.followCounter > 0) {
                this.followCounter--;
            } else {
                this.moveTowardsTarget();
            }
        } else {
            this.moveTowardsTarget();
        }
    }

 

    private void updateCounters() {
        if (this.attackTickCounter > 0) {
            this.attackTickCounter--;
        }

        if (this.lastDamageDealtCounter > 0) {
            this.lastDamageDealtCounter--;
        } else {
            this.lastDamageDealt = null;
        }

        if (this.hpBarCounter > 0) {
            this.hpBarCounter--;
        } else {
            this.isHpBarShown = false;
        }

    }

    public void resetNpc() {
        this.targetTile = null;
        this.newTargetTile = null;
        this.targetedEntityID = null;
        this.targetObjectID = null;
        this.currentHitpoints = ExperienceUtils.getLevelByExp(this.skills[SkillUtils.HITPOINTS]);
        this.worldX = this.originalWorldX;
        this.worldY = this.originalWorldY;
        this.tickCounter = 0;
        this.followCounter = 0;
    }

    public int getCombatLevel() {
        int hitpointsLevel = ExperienceUtils.getLevelByExp(skills[SkillUtils.HITPOINTS]);
        int attackLevel = ExperienceUtils.getLevelByExp(skills[SkillUtils.ATTACK]);
        int strengthLevel = ExperienceUtils.getLevelByExp(skills[SkillUtils.STRENGTH]);
        int defenceLevel = ExperienceUtils.getLevelByExp(skills[SkillUtils.DEFENCE]);

        double base = 0.25 * (defenceLevel + hitpointsLevel);
        double melee = 0.325 * (attackLevel + strengthLevel);

        return (int) (base + melee);
    }

}
