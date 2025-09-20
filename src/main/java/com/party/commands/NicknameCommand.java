package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NicknameCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public NicknameCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsa: /party nickname <player> <nickname>");
            return true;
        }

        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cSolo il Leader può impostare nickname!");
            return true;
        }

        Player target = player.getServer().getPlayer(args[1]);
        if (target == null || !party.getMembers().containsKey(target.getUniqueId())) {
            player.sendMessage("§cQuel giocatore non è nel tuo party!");
            return true;
        }

        String nickname = ChatColor.translateAlternateColorCodes('&', args[2]);
        party.getNicknames().put(target.getUniqueId(), nickname);

        player.sendMessage("§aHai impostato il nickname di §e" + target.getName() + " §ain §r" + nickname);
        target.sendMessage("§aIl tuo nickname nel party è ora §r" + nickname);

        playerListener.updatePlayerPrefix(target);
        return true;
    }
}