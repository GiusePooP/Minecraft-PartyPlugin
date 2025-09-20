package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KickCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public KickCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party kick <player>");
            return true;
        }

        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cSolo il Leader può kickare membri.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !party.getMembers().containsKey(target.getUniqueId())) {
            player.sendMessage("§cQuel giocatore non è nel tuo party!");
            return true;
        }

        party.removeMember(target.getUniqueId());
        partyManager.updateIndexes(party);
        playerListener.updatePlayerPrefix(target);

        player.sendMessage("§aHai kickato §e" + target.getName() + "§a dal party.");
        target.sendMessage("§cSei stato rimosso dal party.");

        return true;
    }
}