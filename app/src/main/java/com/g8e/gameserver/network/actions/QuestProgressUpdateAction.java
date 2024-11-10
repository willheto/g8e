package com.g8e.gameserver.network.actions;

public class QuestProgressUpdateAction extends Action {

    private QuestProgressUpdateActionData data;

    public QuestProgressUpdateAction(String playerID, QuestProgressUpdateActionData data) {
        this.action = "questProgressUpdate";
        this.playerID = playerID;
        this.data = data;
    }

    public int getQuestID() {
        return data.getQuestID();
    }

    public int getProgress() {
        return data.getProgress();
    }
}
