package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.Role;
import com.party.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RankNameCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public RankNameCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsa: /party rankname <rank> <nome_personalizzato>");
            return true;
        }

        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cSolo il Leader può personalizzare i nomi dei rank!");
            return true;
        }

        Role role;
        try {
            role = Role.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cRuolo non valido!");
            return true;
        }

        String customName = ChatColor.translateAlternateColorCodes('&', args[2]);
        party.getRankNicknames().put(role, customName);

        player.sendMessage("§aHai impostato il nome personalizzato del rank §e" + role.name() + " §ain §r" + customName);
        return true;
    }
}