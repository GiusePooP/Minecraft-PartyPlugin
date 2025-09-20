package com.party;

import com.earth2me.essentials.Essentials;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final PartyManager partyManager;
    private final Essentials essentials;

    public PlayerListener(PartyManager partyManager, Essentials essentials) {
        this.partyManager = partyManager;
        this.essentials = essentials;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        updatePlayerPrefix(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        // Reset del nome in tab list alla logout
        player.setPlayerListName(player.getName());
    }

    public void updatePlayerPrefix(Player player) {
        Party party = partyManager.getPartyByPlayer(player.getUniqueId());
        String baseName;

        // Usa il nickname/displayname di EssentialsX se disponibile
        if (essentials != null) {
            baseName = essentials.getUser(player).getDisplayName();
        } else {
            baseName = player.getName();
        }

        if (party != null) {
            String coloredPrefix = ChatColor.translateAlternateColorCodes('&', "[" + party.getName() + ChatColor.RESET + "] ");
            String fullName = coloredPrefix + baseName;

            // Tab list
            player.setPlayerListName(fullName);

            // Chat (EssentialsXChat usa questo)
            player.setDisplayName(fullName);
        } else {
            player.setPlayerListName(baseName);
            player.setDisplayName(baseName);
        }
    }
}