package com.g8e.gameserver.models;

public class AttackEvent {
    public String attackerID;
    public String targetID;

    public AttackEvent(String attackerID, String targetID) {
        this.attackerID = attackerID;
        this.targetID = targetID;
    }

}
