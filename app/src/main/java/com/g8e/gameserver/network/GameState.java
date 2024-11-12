package com.g8e.gameserver.network;

import java.util.List;
import com.g8e.gameserver.models.AttackEvent;
import com.g8e.gameserver.models.Npc;
import com.g8e.gameserver.models.Player;
import com.g8e.gameserver.models.TalkEvent;
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Item;

public class GameState {
    private List<AttackEvent> tickAttackEvents;
    private List<TalkEvent> tickTalkEvents;
    private List<Player> players;
    private List<Npc> npcs;
    private List<ChatMessage> chatMessages;
    private String playerID;
    private List<Item> items;

    public GameState(List<AttackEvent> tickAttackEvents, List<TalkEvent> tickTalkEvents, List<Player> players,
            List<Npc> npcs,
            List<ChatMessage> chatMessages, List<Item> items, String playerID) {
        this.tickAttackEvents = tickAttackEvents;
        this.tickTalkEvents = tickTalkEvents;
        this.players = players;
        this.npcs = npcs;
        this.playerID = playerID;
        this.chatMessages = chatMessages;
        this.items = items;
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

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<TalkEvent> getTickTalkEvents() {
        return tickTalkEvents;
    }
}