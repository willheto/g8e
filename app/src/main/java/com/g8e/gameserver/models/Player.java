package com.g8e.gameserver.models;

import java.sql.SQLException;
import java.util.List;

import com.g8e.db.CommonQueries;
import com.g8e.db.models.DBPlayer;
import com.g8e.gameserver.World;
import com.g8e.gameserver.models.Quest.Quest;
import com.g8e.gameserver.models.Quest.QuestReward;
import com.g8e.gameserver.util.ExperienceUtils;
import com.g8e.gameserver.util.SkillUtils;
import com.g8e.util.Logger;
import com.google.gson.Gson;
import com.g8e.gameserver.network.actions.Action;
import com.g8e.gameserver.network.actions.ChangeAppearanceAction;
import com.g8e.gameserver.network.actions.ChangeAttackStyleAction;
import com.g8e.gameserver.network.actions.DropItemAction;
import com.g8e.gameserver.network.actions.EatItemAction;
import com.g8e.gameserver.network.actions.PlayerAttackMove;
import com.g8e.gameserver.network.actions.PlayerMove;
import com.g8e.gameserver.network.actions.PlayerTakeMoveAction;
import com.g8e.gameserver.network.actions.PlayerTalkMoveAction;
import com.g8e.gameserver.network.actions.QuestProgressUpdateAction;
import com.g8e.gameserver.network.actions.RemoveItemFromInventoryAction;
import com.g8e.gameserver.network.actions.UnwieldAction;
import com.g8e.gameserver.network.actions.UseItemAction;
import com.g8e.gameserver.network.actions.WieldItemAction;
import com.g8e.gameserver.tile.TilePosition;

public class Player extends Combatant {
    public int accountID;

    public int[] inventory = new int[20];
    public int[] questProgress = new int[10];
    public int influence;
    public int skinColor;
    public int hairColor;
    public int shirtColor;

    public Player(World world, DBPlayer dbPlayer, String uniquePlayerID, String username, int accountID) {
        super(uniquePlayerID, 0, world, dbPlayer.getWorldX(), dbPlayer.getWorldY(), username,
                "That's " + username + "!", 0);
        this.accountID = accountID;
        this.currentHitpoints = ExperienceUtils.getLevelByExp(dbPlayer.getHitpointsExperience());

        this.loadPlayerSkills(dbPlayer);
        this.combatLevel = this.getCombatLevel();
        this.weapon = dbPlayer.getWeapon();
        this.loadPlayerInventory(dbPlayer);
        this.loadQuestProgress(dbPlayer);
        this.attackStyle = "attack";
        this.skinColor = dbPlayer.getSkinColor();
        this.hairColor = dbPlayer.getHairColor();
        this.shirtColor = dbPlayer.getShirtColor();
    }

    private void loadQuestProgress(DBPlayer dbPlayer) {
        for (int i = 0; i < dbPlayer.getQuestProgress().length; i++) {
            this.questProgress[i] = dbPlayer.getQuestProgress()[i];

            Quest quest = this.world.questsManager.getQuestByID(i);

            if (dbPlayer.getQuestProgress()[i] == 100 && quest != null) {
                influence += quest.getRewards().getInfluenceReward();
            }
        }

    }

    public void update() {
        this.updateCounters();

        if (this.targetedEntityID != null) {
            if (isOneStepAwayFromTarget()) {
                if (goalAction == 2) {
                    Entity entity = this.world.getEntityByID(((Combatant) this).targetedEntityID);
                    if (entity != null && entity instanceof Combatant) {
                        ((Combatant) this).attackEntity((Combatant) entity);
                        this.nextTileDirection = null;
                        this.targetTile = null;
                        this.newTargetTile = null;
                        this.targetEntityLastPosition = null;
                        return;
                    }
                } else if (goalAction == 1) {
                    Entity entity = this.world.getEntityByID(this.targetedEntityID);
                    if (entity != null && entity instanceof Npc) {
                        this.nextTileDirection = null;
                        this.targetTile = null;
                        this.newTargetTile = null;
                        this.targetEntityLastPosition = null;
                        this.goalAction = null;
                        this.targetedEntityID = null;
                        TalkEvent talkEvent = new TalkEvent(this.entityID, entity.entityID, entity.entityIndex);
                        this.world.tickTalkEvents.add(talkEvent);
                        return;
                    }

                }
            }
        }

        this.moveTowardsTarget();
    }

    private boolean isOneStepAwayFromTarget() {
        Entity target = this.world.getEntityByID(this.targetedEntityID);
        if (target == null) {
            Logger.printError("Target not found");
            return false;
        }

        if ((Math.abs(this.worldX - target.worldX) == 1 && this.worldY == target.worldY) ||
                (Math.abs(this.worldY - target.worldY) == 1 && this.worldX == target.worldX)) {
            return true;
        }

        return false;
    }

    public void takeItem(String uniqueItemID) {
        Item item = this.world.itemsManager.getItemByUniqueItemID(uniqueItemID);
        int emptySlot = getEmptyInventorySlot();

        if (emptySlot == -1) {
            Logger.printError("No empty inventory slots, dropping item");
            return;
        }

        this.inventory[emptySlot] = item.getItemID();
        this.world.itemsManager.removeItem(item.getUniqueID());
        saveInventory();
    }

    public void saveQuestProgress() {
        Gson gson = new Gson();
        String questProgressString = gson.toJson(this.questProgress);
        try {
            CommonQueries.savePlayerQuestProgressByAccountId(this.accountID, questProgressString);
        } catch (Exception e) {
            Logger.printError("Failed to save quest progress");
        }
    }

    public void savePosition() {
        try {
            CommonQueries.savePlayerPositionByAccountId(this.accountID, this.worldX, this.worldY);
        } catch (Exception e) {
            Logger.printError("Failed to save player position");
        }
    }

    public void saveInventory() {
        Gson gson = new Gson();
        String inventoryString = gson.toJson(this.inventory);
        try {
            CommonQueries.savePlayerInventoryByAccountId(this.accountID, inventoryString);
        } catch (Exception e) {
            Logger.printError("Failed to save inventory");
        }
    }

    public void addXp(int skill, int xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("XP cannot be negative");
        }

        if (xp == 0) {
            return;
        }

        int previousLevel = ExperienceUtils.getLevelByExp(this.skills[skill]);
        this.skills[skill] += xp;
        int currentLevel = ExperienceUtils.getLevelByExp(this.skills[skill]);

        if (this.skills[skill] > 200_000_000) {
            this.skills[skill] = 200_000_000;
        }

        if (currentLevel > previousLevel) {
            String levelUpMessage = "Congratulations, you've just advanced a " + SkillUtils.getSkillNameByNumber(skill)
                    + " level! Your " + SkillUtils.getSkillNameByNumber(skill) + " level is now " + currentLevel + ".";

            long timeSent = System.currentTimeMillis();
            ChatMessage chatMessageModel = new ChatMessage(this.name, levelUpMessage, timeSent, false);
            this.world.chatMessages.add(chatMessageModel);

        }

        this.combatLevel = this.getCombatLevel();
        saveSkillXp(skill);
    }

    public void saveSkillXp(int skill) {
        try {
            CommonQueries.savePlayerXpByAccountId(this.accountID, skill, this.skills[skill]);
        } catch (Exception e) {
            Logger.printError("Failed to save skill xp");
        }
    }

    private void updateInventoryOrder(int[] requestedInventory) {
        if (requestedInventory.length != this.inventory.length) {
            Logger.printError("Inventory length mismatch");
            return;
        }

        // check if inventory has same items but in different order
        for (int inventory : this.inventory) {
            boolean found = false;
            for (int requestedItem : requestedInventory) {
                if (inventory == requestedItem) {
                    continue;
                }
            }

            if (!found) {
                Logger.printError("Inventory mismatch");
                return;
            }
        }

        this.inventory = requestedInventory;
    }

    public void setTickActions(List<Action> actions) {
        for (Action action : actions) {

            if (action instanceof ChangeAppearanceAction) {
                ChangeAppearanceAction changeAppearanceAction = (ChangeAppearanceAction) action;
                this.skinColor = changeAppearanceAction.getSkinColor();
                this.hairColor = changeAppearanceAction.getHairColor();
                this.shirtColor = changeAppearanceAction.getShirtColor();
                try {
                    CommonQueries.savePlayerAppearanceByAccountId(
                            this.accountID, this.skinColor, this.hairColor, this.shirtColor);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            if (action instanceof PlayerMove) {
                PlayerMove playerMove = (PlayerMove) action;
                this.newTargetTile = new TilePosition(playerMove.getX(), playerMove.getY());
                this.targetItemID = null;
                this.targetedEntityID = null;
                this.goalAction = null;
            }

            if (action instanceof PlayerAttackMove) {
                PlayerAttackMove playerAttackMove = (PlayerAttackMove) action;
                Entity npc = this.world.getEntityByID(playerAttackMove.getEntityID());
                this.newTargetTile = new TilePosition(npc.worldX, npc.worldY);
                this.targetedEntityID = playerAttackMove.getEntityID();
                this.goalAction = 2; // Attack action TODO
            }

            if (action instanceof DropItemAction) {
                DropItemAction dropItemAction = (DropItemAction) action;
                this.dropItem(dropItemAction.getInventoryIndex());
            }

            if (action instanceof WieldItemAction) {
                WieldItemAction wieldItemAction = (WieldItemAction) action;
                this.wieldWeapon(wieldItemAction.getInventoryIndex());
            }

            if (action instanceof PlayerTakeMoveAction) {
                PlayerTakeMoveAction playerTakeMoveAction = (PlayerTakeMoveAction) action;
                this.handlePlayerTakeMove(playerTakeMoveAction.getUniqueItemID());
                this.goalAction = null;
            }

            if (action instanceof UseItemAction) {
                UseItemAction useItemAction = (UseItemAction) action;
                this.useItem(useItemAction.getItemID(), useItemAction.getTargetID());
            }

            if (action instanceof UnwieldAction) {
                UnwieldAction unwieldAction = (UnwieldAction) action;
                this.unwieldItem(unwieldAction.getInventoryIndex());
            }

            if (action instanceof EatItemAction) {
                EatItemAction eatItemAction = (EatItemAction) action;
                this.eatItem(eatItemAction.getInventoryIndex());
            }

            if (action instanceof QuestProgressUpdateAction) {
                QuestProgressUpdateAction questProgressUpdateAction = (QuestProgressUpdateAction) action;
                this.questProgressUpdate(questProgressUpdateAction.getQuestID(),
                        questProgressUpdateAction.getProgress());
            }

            if (action instanceof PlayerTalkMoveAction) {
                PlayerTalkMoveAction playerTalkMoveAction = (PlayerTalkMoveAction) action;

                Entity entity = this.world.getEntityByID(playerTalkMoveAction.getEntityID());
                if (entity != null) {
                    this.targetedEntityID = playerTalkMoveAction.getEntityID();
                    this.goalAction = 1;
                    this.newTargetTile = new TilePosition(entity.worldX, entity.worldY);
                }
            }

            if (action instanceof ChangeAttackStyleAction) {
                ChangeAttackStyleAction changeAttackStyleAction = (ChangeAttackStyleAction) action;
                this.attackStyle = changeAttackStyleAction.getAttackStyle();
            }

            if(action instanceof RemoveItemFromInventoryAction) {
                RemoveItemFromInventoryAction removeItemFromInventoryAction = (RemoveItemFromInventoryAction) action;
                int itemID = removeItemFromInventoryAction.getItemID();
                for (int i = 0; i < this.inventory.length; i++) {
                    if (this.inventory[i] == itemID) {
                        this.inventory[i] = 0;
                        saveInventory();
                        break;
                    }
                }
            }
        }
    }

    private void eatItem(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= this.inventory.length) {
            Logger.printError("Invalid inventory index");
            return;
        }

        int itemID = this.inventory[inventoryIndex];
        Item item = this.world.itemsManager.getItemByID(itemID);
        Edible edible = this.world.itemsManager.getEdibleInfoByItemID(itemID);

        if (item == null) {
            Logger.printError("Item not found or not edible");
            return;
        }

        this.inventory[inventoryIndex] = 0;
        this.currentHitpoints += edible.getHealAmount();
        this.world.chatMessages
                .add(new ChatMessage(this.name, "You eat the " + item.getName() + ". " + "It heals some health.",
                        System.currentTimeMillis(),
                        false));

        if (this.currentHitpoints > ExperienceUtils.getLevelByExp(this.skills[SkillUtils.HITPOINTS])) {
            this.currentHitpoints = ExperienceUtils.getLevelByExp(this.skills[SkillUtils.HITPOINTS]);
        }

    }

    private void useItem(int itemID, int targetID) {
        this.world.chatMessages
                .add(new ChatMessage(this.name, "Nothing interesting happens.", System.currentTimeMillis(), false));

    }

    private void handlePlayerTakeMove(String uniqueItemID) {
        Item item = this.world.itemsManager.getItemByUniqueItemID(uniqueItemID);
        if (item == null) {
            this.world.chatMessages
                    .add(new ChatMessage(this.name, "Too late, it's gone!", System.currentTimeMillis(), false));
            return;
        }

        if (item.getWorldX() == this.worldX && item.getWorldY() == this.worldY) {
            this.takeItem(uniqueItemID);
            return;
        }

        this.targetItemID = uniqueItemID;
        this.newTargetTile = new TilePosition(item.getWorldX(), item.getWorldY());
    }

    private void questProgressUpdate(int questID, int progress) {
        if (questID < 0 || questID >= this.questProgress.length) {
            Logger.printError("Invalid quest ID");
            return;
        }

        this.questProgress[questID] = progress;
        saveQuestProgress();

        if (progress == 100) {
            ChatMessage chatMessage = new ChatMessage(this.name, "Congratulations, you've completed a quest!",
                    System.currentTimeMillis(), false);
            this.world.chatMessages.add(chatMessage);

            Quest quest = this.world.questsManager.getQuestByID(questID);
            QuestReward reward = quest.getRewards();

            int[] skillRewards = reward.getSkillRewards();
            for (int i = 0; i < skillRewards.length; i++) {
                this.addXp(i, skillRewards[i]);
            }

            int[] itemRewards = reward.getItemRewards();
            for (int itemID : itemRewards) {
                int emptySlot = getEmptyInventorySlot();

                if (emptySlot == -1) {
                    Logger.printError("No empty inventory slots, dropping item");
                    this.world.itemsManager.spawnItem(this.worldX, this.worldY, itemID);
                    return;
                }

                this.inventory[emptySlot] = itemID;
                saveInventory();
            }

        }
    }

    private int getEmptyInventorySlot() {
        for (int i = 0; i < this.inventory.length; i++) {
            if (this.inventory[i] == 0) {
                return i;
            }
        }

        return -1;
    }

    public void saveWeapon() {
        try {
            CommonQueries.savePlayerWeaponByAccountId(this.accountID, this.weapon);
        } catch (Exception e) {
            Logger.printError("Failed to save weapon");
        }
    }

    private void dropItem(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= this.inventory.length) {
            Logger.printError("Invalid inventory index");
            return;
        }

        int itemID = this.inventory[inventoryIndex];
        if (itemID == 0) {
            Logger.printError("No item to drop");
            return;
        }

        if (this.weapon != null && this.weapon == inventoryIndex) {
            this.weapon = null;
            saveWeapon();
        }

        this.inventory[inventoryIndex] = 0;
        saveInventory();

        this.world.itemsManager.spawnItem(this.worldX, this.worldY, itemID);
    }

    private void unwieldItem(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= this.inventory.length) {
            Logger.printError("Invalid inventory index");
            return;
        }

        int itemID = this.inventory[inventoryIndex];
        Item item = this.world.itemsManager.getItemByID(itemID);

        if (item == null) {
            Logger.printError("Item not found");
            return;
        }

        if (item.isWieldable() == false) {
            Logger.printError("Item is not wieldable");
            return;
        }

        if (this.weapon == inventoryIndex) {
            this.weapon = null;
            saveWeapon();
        }

    }

    private void wieldWeapon(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= this.inventory.length) {
            Logger.printError("Invalid inventory index");
            return;
        }

        int itemID = this.inventory[inventoryIndex];
        Item item = this.world.itemsManager.getItemByID(itemID);

        if (item == null) {
            Logger.printError("Item not found");
            return;
        }

        if (item.isWieldable() == false) {
            Logger.printError("Item is not wieldable");
            return;
        }

        this.weapon = inventoryIndex;
        saveWeapon();

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

    private void loadPlayerSkills(DBPlayer player) {
        this.skills[SkillUtils.ATTACK] = player.getAttackExperience();
        this.skills[SkillUtils.STRENGTH] = player.getStrengthExperience();
        this.skills[SkillUtils.DEFENCE] = player.getDefenceExperience();
        this.skills[SkillUtils.HITPOINTS] = player.getHitpointsExperience();
    }

    private void loadPlayerInventory(DBPlayer player) {

        this.inventory = new int[20];
        for (int i = 0; i < player.getInventory().length; i++) {
            this.inventory[i] = player.getInventory()[i];
        }
    }

    public void killPlayer() {
        this.currentHitpoints = ExperienceUtils.getLevelByExp(this.skills[3]);
        this.worldX = this.originalWorldX;
        this.worldY = this.originalWorldY;
        this.targetTile = null;
        this.newTargetTile = null;
        this.targetedEntityID = null;
        this.targetItemID = null;
        this.hpBarCounter = 0;
        this.lastDamageDealt = null;
        this.lastDamageDealtCounter = 0;
        this.attackTickCounter = 0;
        this.currentPath = null;
        this.nextTileDirection = null;
        this.goalAction = null;
    }

}
