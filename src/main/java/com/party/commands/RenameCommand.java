package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RenameCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public RenameCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party rename <nuovo_nome>");
            return true;
        }

        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cSolo il Leader può rinominare il party.");
            return true;
        }

        String newName = ChatColor.translateAlternateColorCodes('&', args[1]);
        party.setName(newName);
        player.sendMessage("§aHai rinominato il party in §r" + newName);

        // Aggiorna tab list dei membri
        for (UUID memberId : party.getMembers().keySet()) {
            Player member = player.getServer().getPlayer(memberId);
            if (member != null && member.isOnline()) {
                playerListener.updatePlayerPrefix(member);
            }
        }

        partyManager.updateIndexes(party);
        return true;
    }
}