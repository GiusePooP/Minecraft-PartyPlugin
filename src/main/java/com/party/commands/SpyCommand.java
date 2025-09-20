package com.party.commands;

import org.bukkit.entity.Player;

public class SpyCommand {

    private final SpyManager spyManager;

    public SpyCommand(SpyManager spyManager) {
        this.spyManager = spyManager;
    }

    public boolean execute(Player player) {
        if (!player.isOp()) {
            player.sendMessage("§cSolo gli operatori possono usare questo comando.");
            return true;
        }

        if (spyManager.isSpying(player)) {
            spyManager.disableSpy(player);
            player.sendMessage("§eSpy disattivato.");
        } else {
            spyManager.enableSpy(player);
            player.sendMessage("§aSpy attivato. Ora riceverai tutte le chat dei party.");
        }

        return true;
    }
}