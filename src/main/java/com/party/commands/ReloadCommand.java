package com.party.commands;

import com.party.PartyManager;
import org.bukkit.entity.Player;

public class ReloadCommand {

    private final PartyManager partyManager;

    public ReloadCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public boolean execute(Player player) {
        if (!player.isOp()) {
            player.sendMessage("§cSolo gli operatori del server possono usare questo comando.");
            return true;
        }

        partyManager.loadParties();
        player.sendMessage("§aTutti i dati dei party sono stati ricaricati correttamente.");
        return true;
    }
}