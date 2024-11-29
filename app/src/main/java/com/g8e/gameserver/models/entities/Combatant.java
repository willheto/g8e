package com.g8e.gameserver.models.entities;

import com.g8e.gameserver.World;
import com.g8e.gameserver.enums.Direction;
import com.g8e.gameserver.models.events.AttackEvent;
import com.g8e.gameserver.models.objects.Wieldable;
import com.g8e.gameserver.util.CombatUtils;
import com.g8e.gameserver.util.SkillUtils;
import com.g8e.util.Logger;

public abstract class Combatant extends Entity {
    public int[] skills = new int[4];
    public int currentHitpoints;
    public String targetedEntityID = null;
    public boolean isInCombat;
    public Integer lastDamageDealt = null;

    public int lastDamageDealtCounter;
    public int attackTickCounter;
    public int isInCombatCounter;
    public int combatLevel;
    public String attackStyle;

    public Integer weapon = null;
    public Integer helmet = null;
    public Integer shield = null;
    public Integer bodyArmor = null;
    public Integer legArmor = null;
    public Integer gloves = null;
    public Integer boots = null;
    public Integer neckwear = null;
    public Integer ring = null;

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

        if (entity.isDying) {
            return;
        }

        // always face the target
        int entityX = entity.worldX;
        int entityY = entity.worldY;

        if (entityX < this.worldX) {
            this.facingDirection = Direction.LEFT;
        } else if (entityX > this.worldX) {
            this.facingDirection = Direction.RIGHT;
        } else if (entityY < this.worldY) {
            this.facingDirection = Direction.UP;
        } else if (entityY > this.worldY) {
            this.facingDirection = Direction.DOWN;
        }

        Wieldable weapon = null;
        if (this instanceof Player && this.weapon != null) {
            weapon = this.world.itemsManager.getWieldableInfoByItemID(((Player) this).inventory[this.weapon]);
        }

        this.attackTickCounter = weapon != null ? weapon.getAttackSpeed() : 4;

        int accuracyBonus = weapon != null ? weapon.getAccuracyBonus() : 0;
        int strengthBonus = weapon != null ? weapon.getStrengthBonus() : 0;
        int attackDamage = CombatUtils.getAttackDamage(this.skills[SkillUtils.ATTACK], this.skills[SkillUtils.STRENGTH],
                entity.skills[SkillUtils.DEFENCE],
                accuracyBonus,
                strengthBonus,
                0);

        entity.currentHitpoints -= attackDamage;
        if (entity.currentHitpoints < 0) {
            entity.currentHitpoints = 0;
        }
        int multiplier = 1;

        if (this instanceof Player) {
            if (this.attackStyle.equals("attack")) {
                ((Player) this).addXp(SkillUtils.ATTACK, (4 * attackDamage) * multiplier);
            } else if (this.attackStyle.equals("strength")) {
                ((Player) this).addXp(SkillUtils.STRENGTH, (4 * attackDamage) * multiplier);
            } else if (this.attackStyle.equals("defence")) {
                ((Player) this).addXp(SkillUtils.DEFENCE, (4 * attackDamage) * multiplier);
            }
            ((Player) this).addXp(SkillUtils.HITPOINTS, (1 * attackDamage) * multiplier);

        }

        entity.lastDamageDealt = attackDamage;
        entity.lastDamageDealtCounter = 1;
        entity.isInCombatCounter = 20;
        entity.isInCombat = true;

        AttackEvent attackEvent = new AttackEvent(this.entityID,
                entity.entityID);
        this.world.tickAttackEvents.add(attackEvent);

        if (entity.currentHitpoints <= 0) {
            this.clearTarget();
            if (entity instanceof Npc) {
                ((Npc) entity).isDying = true;
                ((Npc) entity).nextTileDirection = null;
                ((Player) this).runQuestScriptsForKill(entity.entityIndex);

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

    protected void clearTarget() {
        this.targetedEntityID = null;
        this.targetTile = null;
        this.newTargetTile = null;
    }

}