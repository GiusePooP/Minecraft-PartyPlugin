package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.UUID;

public class InfoCommand {

    private final PartyManager partyManager;

    public InfoCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public boolean execute(Player player, String[] args) {
        Party targetParty;

        if (args.length == 1) {
            targetParty = partyManager.getPartyByPlayer(player.getUniqueId());
            if (targetParty == null) {
                player.sendMessage("§cNon sei in nessun party.");
                return true;
            }
        } else {
            targetParty = partyManager.getPartyByName(args[1]);
            if (targetParty == null) {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer != null) {
                    targetParty = partyManager.getPartyByPlayer(targetPlayer.getUniqueId());
                    if (targetParty == null) {
                        player.sendMessage("§cQuel player non è in nessun party.");
                        return true;
                    }
                } else {
                    player.sendMessage("§cNessun party o player trovato con quel nome.");
                    return true;
                }
            }
        }

        String coloredName = ChatColor.translateAlternateColorCodes('&', targetParty.getName());
        player.sendMessage("§6Party: §r" + coloredName);

        player.sendMessage("§7Leader: §f" + Bukkit.getOfflinePlayer(targetParty.getLeader()).getName());
        player.sendMessage("§7Descrizione: §f" + ChatColor.translateAlternateColorCodes('&', targetParty.getDescription()));
        player.sendMessage("§7Membri:");

        for (UUID memberId : targetParty.getMembers().keySet()) {
            Role role = targetParty.getMembers().get(memberId);
            String nick = targetParty.getNicknames().getOrDefault(memberId,
                    targetParty.getRankNicknames().getOrDefault(role, role.name()));
            nick = ChatColor.translateAlternateColorCodes('&', nick);

            player.sendMessage(" - " + Bukkit.getOfflinePlayer(memberId).getName() + " §7(" + nick + ")");
        }

        return true;
    }
}