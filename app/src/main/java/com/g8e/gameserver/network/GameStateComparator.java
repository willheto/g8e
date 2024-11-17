package com.g8e.gameserver.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.g8e.gameserver.models.ChatMessage;
import com.g8e.gameserver.models.Item;
import com.g8e.gameserver.models.events.AttackEvent;
import com.g8e.gameserver.models.events.TalkEvent;
import com.g8e.gameserver.models.events.TradeEvent;
import com.g8e.gameserver.network.dataTransferModels.DTONpc;
import com.g8e.gameserver.network.dataTransferModels.DTOPlayer;

public class GameStateComparator {

    public static GameState getChangedGameState(GameState oldState, GameState newState) {
        List<AttackEvent> changedAttackEvents = newState.getTickAttackEvents();
        List<TalkEvent> changedTalkEvents = newState.getTickTalkEvents();
        List<TradeEvent> changedTradeEvents = newState.getTickTradeEvents();
        List<DTOPlayer> changedPlayers = getChangedPlayers(oldState.getPlayers(), newState.getPlayers());
        List<DTONpc> changedNpcs = getChangedNpcs(oldState.getNpcs(), newState.getNpcs());
        List<ChatMessage> changedChatMessages = newState.getChatMessages();
        List<Item> changedItems = newState.getItems();

        String changedPlayerID = !Objects.equals(oldState.getPlayerID(), newState.getPlayerID())
                ? newState.getPlayerID()
                : null;

        return new GameState(
                changedAttackEvents,
                changedTalkEvents,
                changedTradeEvents,
                changedPlayers.isEmpty() ? Collections.emptyList() : changedPlayers,
                changedNpcs.isEmpty() ? Collections.emptyList() : changedNpcs,
                changedChatMessages,
                changedItems,
                changedPlayerID,
                newState.getOnlinePlayers(),
                newState.getShops());

    }

    private static List<DTONpc> getChangedNpcs(List<DTONpc> oldNpcs, List<DTONpc> newNpcs) {
        List<DTONpc> changedNpcs = new ArrayList<>();

        // if list differ in length, use newNpcs as the base
        if (oldNpcs.size() != newNpcs.size()) {
            return newNpcs;
        }

        for (int i = 0; i < Math.max(oldNpcs.size(), newNpcs.size()); i++) {
            DTONpc oldNpc = i < oldNpcs.size() ? oldNpcs.get(i) : null;
            DTONpc newNpc = i < newNpcs.size() ? newNpcs.get(i) : null;
            // Add only if there is a change between the old npc and new npc
            if (!newNpc.equals(oldNpc)) {
                changedNpcs.add(newNpc);
            }
        }
        return changedNpcs;
    }

    private static List<DTOPlayer> getChangedPlayers(List<DTOPlayer> oldPlayers, List<DTOPlayer> newPlayers) {

        if (oldPlayers.size() != newPlayers.size()) {
            return newPlayers;
        }

        List<DTOPlayer> changedPlayers = new ArrayList<>();
        for (int i = 0; i < Math.max(oldPlayers.size(), newPlayers.size()); i++) {
            DTOPlayer oldPlayer = i < oldPlayers.size() ? oldPlayers.get(i) : null;
            DTOPlayer newPlayer = i < newPlayers.size() ? newPlayers.get(i) : null;
            if (oldPlayer == null || newPlayer == null) {
                continue;
            }
            if (!newPlayer.equals(oldPlayer)) {
                changedPlayers.add(newPlayer);
            }
        }
        return changedPlayers;
    }
}