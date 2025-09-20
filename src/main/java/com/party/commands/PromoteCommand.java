package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.Role;
import com.party.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PromoteCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public PromoteCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsa: /party promote <player> <rank>");
            return true;
        }

        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cSolo il Leader può promuovere membri.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !party.getMembers().containsKey(target.getUniqueId())) {
            player.sendMessage("§cQuel giocatore non è nel tuo party!");
            return true;
        }

        Role newRole;
        try {
            newRole = Role.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cRuolo non valido!");
            return true;
        }

        party.addMember(target.getUniqueId(), newRole);
        player.sendMessage("§aHai promosso §e" + target.getName() + "§a a §e" + newRole.name() + "§a.");
        target.sendMessage("§aSei stato promosso a §e" + newRole.name() + "§a nel party!");

        playerListener.updatePlayerPrefix(target);

        return true;
    }
}