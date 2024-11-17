package com.g8e.gameserver.network;

import java.util.List;

import com.g8e.gameserver.network.dataTransferModels.DTONpc;
import com.g8e.gameserver.network.dataTransferModels.DTOPlayer;
import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Item;
import com.g8e.gameserver.models.Shop;
import com.g8e.gameserver.models.events.AttackEvent;
import com.g8e.gameserver.models.events.TalkEvent;
import com.g8e.gameserver.models.events.TradeEvent;

public class GameState {
    private List<AttackEvent> tickAttackEvents;
    private List<TalkEvent> tickTalkEvents;
    public List<TradeEvent> tickTradeEvents;
    private List<DTOPlayer> players;
    private List<DTONpc> npcs;
    private List<ChatMessage> chatMessages;
    private String playerID;
    private List<Item> items;
    private List<String> onlinePlayers;
    private Shop[] shops;

    public GameState(List<AttackEvent> tickAttackEvents, List<TalkEvent> tickTalkEvents,
            List<TradeEvent> tickTradeEvents,
            List<DTOPlayer> players,
            List<DTONpc> npcs,
            List<ChatMessage> chatMessages, List<Item> items, String playerID, List<String> onlinePlayers,
            Shop[] shops) {
        this.tickAttackEvents = tickAttackEvents;
        this.tickTalkEvents = tickTalkEvents;
        this.tickTradeEvents = tickTradeEvents;
        this.players = players;
        this.npcs = npcs;
        this.playerID = playerID;
        this.chatMessages = chatMessages;
        this.items = items;
        this.onlinePlayers = onlinePlayers;
        this.shops = shops;
    }

    public List<TradeEvent> getTickTradeEvents() {
        return tickTradeEvents;
    }

    public Shop[] getShops() {
        return shops;
    }

    public List<String> getOnlinePlayers() {
        return onlinePlayers;
    }

    public List<DTOPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<DTOPlayer> players) {
        this.players = players;
    }

    public List<DTONpc> getNpcs() {
        return npcs;
    }

    public void setNpcs(List<DTONpc> npcs) {
        this.npcs = npcs;
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