package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.PlayerListener;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DisbandCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public DisbandCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player) {
        UUID uuid = player.getUniqueId();
        Party disbandParty = partyManager.getPartyByPlayer(uuid);

        if (disbandParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }
        if (!disbandParty.getLeader().equals(uuid)) {
            player.sendMessage("§cSolo il Leader può disbandare il party.");
            return true;
        }

        for (UUID memberId : disbandParty.getMembers().keySet()) {
            Player member = player.getServer().getPlayer(memberId);
            if (member != null && member.isOnline()) {
                playerListener.updatePlayerPrefix(member);
                if (!memberId.equals(uuid))
                    member.sendMessage("§cIl party è stato disbandato dal Leader.");
            }
        }

        partyManager.removeParty(disbandParty.getId());
        playerListener.updatePlayerPrefix(player);
        player.sendMessage("§cHai disbandato il tuo party.");
        return true;
    }
}