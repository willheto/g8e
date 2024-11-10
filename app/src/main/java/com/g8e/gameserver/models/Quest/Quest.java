package com.g8e.gameserver.models.Quest;

public class Quest {
    private int questID;
    private String name;
    private String description;
    private int[] skillRequirements;
    private QuestReward rewards;

    public Quest(int questID, String name, String description, int[] skillRequirements, QuestReward rewards) {
        this.questID = questID;
        this.name = name;
        this.description = description;
        this.skillRequirements = skillRequirements;
        this.rewards = rewards;
    }

    public int getQuestID() {
        return questID;
    }

    public void setQuestID(int questID) {
        this.questID = questID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int[] getSkillRequirements() {
        return skillRequirements;
    }

    public void setSkillRequirements(int[] skillRequirements) {
        this.skillRequirements = skillRequirements;
    }

    public QuestReward getRewards() {
        return rewards;
    }

    public void setRewards(QuestReward rewards) {
        this.rewards = rewards;
    }

}
