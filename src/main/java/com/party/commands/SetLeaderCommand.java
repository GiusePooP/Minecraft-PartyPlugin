package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SetLeaderCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public SetLeaderCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party setleader <player>");
            return true;
        }

        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cSolo il Leader può trasferire il comando!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !party.getMembers().containsKey(target.getUniqueId())) {
            player.sendMessage("§cQuel giocatore non è nel tuo party!");
            return true;
        }

        party.setLeader(target.getUniqueId());
        player.sendMessage("§aHai trasferito il ruolo di leader a §e" + target.getName());
        target.sendMessage("§aSei ora il leader del party!");
        playerListener.updatePlayerPrefix(player);
        playerListener.updatePlayerPrefix(target);

        return true;
    }
}