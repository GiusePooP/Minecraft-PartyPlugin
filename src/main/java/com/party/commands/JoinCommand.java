package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import com.party.Role;
import com.party.PlayerListener;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JoinCommand {

    private final PartyManager partyManager;
    private final PlayerListener playerListener;

    public JoinCommand(PartyManager partyManager, PlayerListener playerListener) {
        this.partyManager = partyManager;
        this.playerListener = playerListener;
    }

    public boolean execute(Player player) {
        UUID uuid = player.getUniqueId();
        Party inviteParty = partyManager.getValidInvite(uuid);

        if (inviteParty == null) {
            player.sendMessage("§cNon sei stato invitato in nessun party o l'invito è scaduto!");
            return true;
        }

        partyManager.addMember(inviteParty, uuid, Role.MEMBER);
        player.sendMessage("§aSei entrato nel party §r" + inviteParty.getName());
        inviteParty.getMembers().keySet().forEach(memberId -> {
            Player member = player.getServer().getPlayer(memberId);
            if (member != null && !member.getUniqueId().equals(uuid)) {
                member.sendMessage("§e" + player.getName() + " §aè entrato nel party!");
            }
        });

        playerListener.updatePlayerPrefix(player);
        partyManager.removeInvite(uuid);
        return true;
    }
}