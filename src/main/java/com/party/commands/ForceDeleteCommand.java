package com.party.commands;

import com.party.Party;
import com.party.PartyManager;
import org.bukkit.entity.Player;

public class ForceDeleteCommand {

    private final PartyManager partyManager;

    public ForceDeleteCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public boolean execute(Player player, String[] args) {
        if (!player.isOp()) {
            player.sendMessage("§cSolo gli operatori possono usare questo comando.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUso corretto: /party forcedelete <nome>");
            return true;
        }

        String name = args[1];
        Party party = partyManager.getPartyByName(name);

        if (party == null) {
            player.sendMessage("§cNessun party trovato con nome §f" + name);
            return true;
        }

        partyManager.disbandParty(party);
        player.sendMessage("§aParty §f" + name + " §adisbandato con successo.");
        return true;
    }
}
