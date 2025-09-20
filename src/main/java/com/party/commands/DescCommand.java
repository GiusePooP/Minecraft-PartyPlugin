package com.party.commands;

import com.party.Party;
import com.party.PartyManager;
import com.party.Role;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DescCommand {

    private final PartyManager partyManager;

    public DescCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public boolean execute(Player player, String[] args) {
        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party.");
            return true;
        }

        Role role = party.getMembers().get(player.getUniqueId());
        if (role != Role.LEADER && role != Role.MODERATOR) {
            player.sendMessage("§cSolo leader e moderatori possono modificare la descrizione.");
            return true;
        }

        if (args.length < 2) {
            // reset della descrizione se non è stato fornito testo
            party.setDescription("");
            player.sendMessage("§eDescrizione del party resettata.");
        } else {
            // unisci tutti gli argomenti come nuova descrizione
            String desc = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            party.setDescription(desc);
            player.sendMessage("§aDescrizione del party aggiornata: " + ChatColor.translateAlternateColorCodes('&', desc));
        }

        partyManager.saveParties();
        return true;
    }
}