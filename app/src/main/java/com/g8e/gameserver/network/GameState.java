package com.g8e.gameserver.network;

import java.util.List;

import com.g8e.gameserver.models.Npc;
import com.g8e.gameserver.models.Player;

public class GameState {
    private int tickUpdateTime;
    private List<Player> players;
    private List<Npc> npcs;
    private String playerID;

    public GameState(int tickUpdateTime, List<Player> players, List<Npc> npcs, String playerID) {
        this.tickUpdateTime = tickUpdateTime;
        this.players = players;
        this.npcs = npcs;
        this.playerID = playerID;
    }

    public int getTickUpdateTime() {
        return tickUpdateTime;
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

}
