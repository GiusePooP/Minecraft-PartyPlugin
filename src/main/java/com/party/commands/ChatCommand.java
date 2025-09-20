package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatCommand {

    private final PartyManager partyManager;

    public ChatCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public boolean execute(Player player) {
        UUID uuid = player.getUniqueId();
        Party party = partyManager.getPartyByPlayer(uuid);
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party.");
            return true;
        }

        if (party.getChatToggled().contains(uuid)) {
            party.getChatToggled().remove(uuid);
            player.sendMessage("§eHai disattivato la chat privata del party.");
        } else {
            party.getChatToggled().add(uuid);
            player.sendMessage("§aHai attivato la chat privata del party. Tutti i messaggi saranno visibili solo ai membri del party.");
        }

        return true;
    }
}