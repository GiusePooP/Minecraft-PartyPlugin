package com.party;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.party.commands.SpyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatHandler implements Listener {

    private final JavaPlugin plugin;
    private final PartyManager partyManager;
    private final SpyManager spyManager;
    private final Essentials essentials;

    public ChatHandler(JavaPlugin plugin, PartyManager partyManager, Essentials essentials, SpyManager spyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.essentials = essentials;
        this.spyManager = spyManager;
    }

    private String getDisplayName(Player p) {
        try {
            if (essentials != null) {
                User user = essentials.getUser(p);
                if (user != null) {
                    String d = user.getDisplayName();
                    if (d != null && !d.isEmpty()) return d;
                }
            }
        } catch (Throwable ignored) {}
        return p.getDisplayName();
    }

    private String colorizeSafe(String str) {
        if (str == null) return "";
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        Party party = partyManager.getPartyByPlayer(uuid);
        final Set<CommandSender> recipients = new HashSet<>(e.getRecipients());

        if (party != null && party.getChatToggled().contains(uuid)) {
            e.setCancelled(true);

            Role role = party.getMembers().get(uuid);
            String personalNickRaw = party.getNicknames().get(uuid);
            String rankNickRaw = party.getRankNicknames().get(role);
            String roleToShowRaw = personalNickRaw != null ? personalNickRaw
                    : (rankNickRaw != null ? rankNickRaw : role.name());

            String roleToShow = colorizeSafe(roleToShowRaw);
            String displayName = getDisplayName(player);
            String tag = ChatColor.GREEN + "[Party " + roleToShow + "]" + ChatColor.RESET + " ";
            final String finalMsg = tag + displayName + ChatColor.RESET + ": " + e.getMessage();

            // invia ai giocatori spy
            spyManager.sendSpyMessage(player, finalMsg);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (CommandSender r : recipients) r.sendMessage(finalMsg);
                Bukkit.getConsoleSender().sendMessage("[Party Chat] " + displayName + " (" + roleToShow + "): " + e.getMessage());
            });
            return;
        }

        if (party != null) {
            e.setCancelled(true);

            String partyPrefix = colorizeSafe("[" + party.getName() + ChatColor.RESET + "]") + ChatColor.RESET + " ";
            String displayName = getDisplayName(player);
            final String finalMsg = partyPrefix + displayName + ChatColor.RESET + ": " + e.getMessage();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (CommandSender r : recipients) r.sendMessage(finalMsg);
                Bukkit.getConsoleSender().sendMessage("[Party] " + displayName + ": " + e.getMessage());
            });
        }
    }
}