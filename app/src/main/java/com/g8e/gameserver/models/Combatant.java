package com.g8e.gameserver.models;

import com.g8e.gameserver.World;
import com.g8e.gameserver.util.CombatUtils;
import com.g8e.gameserver.util.ExperienceUtils;
import com.g8e.gameserver.util.SkillUtils;
import com.g8e.util.Logger;

public abstract class Combatant extends Entity {
    public int[] skills = new int[4];
    public int currentHitpoints;
    public String targetedEntityID = null;
    public boolean isHpBarShown;
    public Integer lastDamageDealt = null;

    public int lastDamageDealtCounter;
    public int attackTickCounter;
    public int hpBarCounter;
    public int combatLevel;
    public int weapon;
    public AttackStyle attackStyle;

    public Combatant(String entityID, int entityIndex, World world, int worldX, int worldY, String name, String examine,
            int type) {
        super(entityID, entityIndex, world, worldX, worldY, name, examine, type);
    }

    public void attackEntity(Combatant entity) {
        if (this.entityID.equals(entity.entityID)) {
            Logger.printError("Player cannot attack itself");
            return;
        }

        if (this.attackTickCounter != 0) {
            return;
        }

        Wieldable weapon = this.world.itemsManager.getWieldableInfoByItemID(this.weapon);

        this.attackTickCounter = weapon != null ? weapon.getAttackSpeed() : 4;

        int accuracyBonus = weapon != null ? weapon.getAccuracyBonus() : 0;
        int attackChance = CombatUtils.calculateHitChance(
                ExperienceUtils.getLevelByExp(this.skills[SkillUtils.ATTACK]),
                ExperienceUtils.getLevelByExp(entity.skills[SkillUtils.DEFENCE]), accuracyBonus, 0);

        int attackBonus = weapon != null ? weapon.getAttackBonus() : 0;
        int attackDamage = CombatUtils.getAttackDamage(this.skills[SkillUtils.STRENGTH],
                attackBonus);

        boolean isDamageDealt = Math.random() * 100 < attackChance;

        if (isDamageDealt) {
            entity.currentHitpoints -= attackDamage;

            if (this instanceof Player) {
                ((Player) this).addXp(SkillUtils.ATTACK, 50);

            }
        }

        entity.lastDamageDealt = isDamageDealt ? attackDamage : 0;
        entity.lastDamageDealtCounter = 3;
        entity.hpBarCounter = 10;
        entity.isHpBarShown = true;

        AttackEvent attackEvent = new AttackEvent(this.entityID, entity.entityID);
        this.world.tickAttackEvents.add(attackEvent);

        if (entity.currentHitpoints <= 0) {
            this.clearTarget();
            if (entity instanceof Npc) {
                ((Npc) entity).isDead = true;

                EntityData entityData = this.world.entitiesManager.getEntityDataByIndex(entity.entityIndex);

                if (entityData.dropTable != null) {
                    for (int i = 0; i < entityData.dropTable.length; i++) {
                        int itemID = entityData.dropTable[i].getItemID();

                        // Roll for drop
                        if (Math.random() < entityData.dropTable[i].getDropChance()) {
                            this.world.itemsManager.spawnItem(entity.worldX, entity.worldY, itemID);
                        }

                    }
                }
            } else if (entity instanceof Player) {
                ((Player) entity).killPlayer();
            }

        }

        this.followCounter = 2;

        if (entity instanceof Npc) {
            if (entity.targetedEntityID == null) {
                entity.targetedEntityID = this.entityID;
                entity.targetTile = null;
                entity.newTargetTile = null;
                entity.targetItemID = null;
            }
        }

    }

    private void clearTarget() {
        this.targetedEntityID = null;
        this.targetTile = null;
        this.newTargetTile = null;
    }

}
