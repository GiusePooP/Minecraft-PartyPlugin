package com.party.commands;

import com.party.PartyPlugin;
import org.bukkit.entity.Player;

public class VersionCommand {

    private final PartyPlugin plugin;

    public VersionCommand(PartyPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean execute(Player player) {
        // Controllo permessi
        if (!player.isOp()) {
            player.sendMessage("§cSolo gli operatori possono usare questo comando.");
            return true;
        }

        String version = plugin.getDescription().getVersion();
        player.sendMessage("§aPartyPlugin versione §e" + version);
        return true;
    }
}