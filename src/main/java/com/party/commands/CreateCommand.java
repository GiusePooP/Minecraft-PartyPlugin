package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.PlayerListener;
import org.bukkit.entity.Player;

public class CreateCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public CreateCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player, String[] args) {
        if (partyManager.getPartyByPlayer(player.getUniqueId()) != null) {
            player.sendMessage("§cSei già in un party!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsa: /party create <nome>");
            return true;
        }

        String name = args[1];
        if (partyManager.getPartyByName(name) != null) {
            player.sendMessage("§cEsiste già un party con questo nome!");
            return true;
        }

        Party party = new Party(java.util.UUID.randomUUID().toString(), name, player.getUniqueId());
        partyManager.addParty(party);
        player.sendMessage("§aHai creato il party §e" + name + "§a!");
        playerListener.updatePlayerPrefix(player);

        return true;
    }
}