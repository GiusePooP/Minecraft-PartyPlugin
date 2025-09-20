package com.party.commands;

import com.party.PartyManager;
import com.party.Party;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ListCommand {

    private final PartyManager partyManager;

    public ListCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public boolean execute(Player player) {
        player.sendMessage("§6Lista party attivi:");
        for (Party p : partyManager.getAllParties()) {
            String coloredName = ChatColor.translateAlternateColorCodes('&', p.getName());
            player.sendMessage(" - " + coloredName + " §7(" + p.getMembers().size() + " membri)");
        }
        return true;
    }
}