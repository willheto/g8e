package com.g8e.gameserver.network.actions;

public class UseItemAction extends Action {

    UseItemActionData data;

    public UseItemAction(String playerID, UseItemActionData useItemActionData) {
        this.action = "useItem";
        this.playerID = playerID;
        this.data = useItemActionData;
    }

    public int getItemID() {
        return data.getItemID();
    }

    public int getTargetID() {
        return data.getTargetID();
    }
}
