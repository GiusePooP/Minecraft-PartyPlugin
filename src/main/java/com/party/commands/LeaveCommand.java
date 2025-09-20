package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.Role;
import com.party.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class LeaveCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public LeaveCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player, UUID uuid) {
        Party leaveParty = partyManager.getPartyByPlayer(uuid);
        if (leaveParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        if (leaveParty.getLeader().equals(uuid)) {
            boolean hasMods = leaveParty.getMembers().values().contains(Role.MODERATOR);
            if (!hasMods) {
                player.sendMessage("§cSei leader e non ci sono moderatori! Usa /party disband o promuovi un membro.");
                return true;
            }
            for (Map.Entry<UUID, Role> entry : leaveParty.getMembers().entrySet()) {
                if (entry.getValue() == Role.MODERATOR) {
                    leaveParty.setLeader(entry.getKey());
                    leaveParty.addMember(entry.getKey(), Role.LEADER);
                    player.sendMessage("§eHai lasciato il party. Nuovo leader: " +
                            Bukkit.getOfflinePlayer(entry.getKey()).getName());
                    Player newLeader = Bukkit.getPlayer(entry.getKey());
                    if (newLeader != null) playerListener.updatePlayerPrefix(newLeader);
                    break;
                }
            }
        }

        leaveParty.removeMember(uuid);
        partyManager.updateIndexes(leaveParty);
        playerListener.updatePlayerPrefix(player);
        player.sendMessage("§aHai lasciato il party.");
        return true;
    }
}
