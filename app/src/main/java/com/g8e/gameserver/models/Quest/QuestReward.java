package com.g8e.gameserver.models.Quest;

public class QuestReward {
    int[] skillRewards;
    int[] itemRewards;

    public QuestReward(int[] skillRewards, int[] itemRewards) {
        this.skillRewards = skillRewards;
        this.itemRewards = itemRewards;
    }

    public int[] getSkillRewards() {
        return skillRewards;
    }

    public void setSkillRewards(int[] skillRewards) {
        this.skillRewards = skillRewards;
    }

    public int[] getItemRewards() {
        return itemRewards;
    }

    public void setItemRewards(int[] itemRewards) {
        this.itemRewards = itemRewards;
    }

}
