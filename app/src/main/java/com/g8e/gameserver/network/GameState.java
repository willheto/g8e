package com.g8e.gameserver.network;

import java.util.List;

import com.g8e.gameserver.models.AttackEvent;
import com.g8e.gameserver.models.Npc;
import com.g8e.gameserver.models.Player;

public class GameState {
    private List<AttackEvent> tickAttackEvents;
    private List<Player> players;
    private List<Npc> npcs;
    private String playerID;

    public GameState(List<AttackEvent> tickAttackEvents, List<Player> players, List<Npc> npcs, String playerID) {
        this.tickAttackEvents = tickAttackEvents;
        this.players = players;
        this.npcs = npcs;
        this.playerID = playerID;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Npc> getNpcs() {
        return npcs;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public List<AttackEvent> getTickAttackEvents() {
        return tickAttackEvents;
    }

    public void setTickAttackEvents(List<AttackEvent> tickAttackEvents) {
        this.tickAttackEvents = tickAttackEvents;
    }

}
