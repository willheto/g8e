package com.g8e.gameserver.network.actions;

public class ChangeAttackStyleAction extends Action {
    private ChangeAttackStyleActionData data;

    public ChangeAttackStyleAction(String playerID, ChangeAttackStyleActionData data) {
        this.action = "changeAttackStyle";
        this.playerID = playerID;
        this.data = data;
    }

    public String getAttackStyle() {
        return data.getAttackStyle();
    }

}
