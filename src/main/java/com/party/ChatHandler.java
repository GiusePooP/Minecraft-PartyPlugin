package com.party;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
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
    private final Essentials essentials; // può essere null se non installato

    public ChatHandler(JavaPlugin plugin, PartyManager partyManager, Essentials essentials) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.essentials = essentials;
    }

    // prende il display name (preferisce Essentials se disponibile)
    private String getDisplayName(Player p) {
        try {
            if (essentials != null) {
                User user = essentials.getUser(p);
                if (user != null) {
                    String d = user.getDisplayName();
                    if (d != null && !d.isEmpty()) return d;
                }
            }
        } catch (Throwable ignored) { }
        return p.getDisplayName();
    }

    // Utility: traduce & -> ChatColor in modo sicuro (se str == null restituisce "")
    private String colorizeSafe(String str) {
        if (str == null) return "";
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        Party party = partyManager.getPartyByPlayer(uuid);

        // copia recipients per usarli in sync (thread-safety)
        final Set<CommandSender> recipients = new HashSet<>(e.getRecipients());

        // -------- Party private chat (toggle) --------
        if (party != null && party.getChatToggled().contains(uuid)) {
            e.setCancelled(true);

            Role role = party.getMembers().get(uuid);

            // priorità: nickname personale -> nickname rank (party specific) -> role.name()
            String personalNickRaw = party.getNicknames().get(uuid); // nickname personale (può contenere &)
            String rankNickRaw = party.getRankNicknames().get(role); // nickname rank (può contenere &)
            String roleToShowRaw = personalNickRaw != null ? personalNickRaw
                    : (rankNickRaw != null ? rankNickRaw : role.name());

            String roleToShow = colorizeSafe(roleToShowRaw); // traduce i & e applica colori
            String displayName = getDisplayName(player);

            // Tag: [Party <RoleName>] colore verde per la parola Party, roleToShow già colorizzato.
            String tag = ChatColor.GREEN + "[Party " + roleToShow + "]" + ChatColor.RESET + " ";

            final String finalMsg = tag + displayName + ChatColor.RESET + ": " + e.getMessage();

            // invio su main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (CommandSender r : recipients) {
                    r.sendMessage(finalMsg);
                }
                Bukkit.getConsoleSender().sendMessage("[Party Chat] " + displayName + " (" + roleToShow + "): " + e.getMessage());
            });

            return;
        }

        // -------- Global chat: se il giocatore è in party, aggiungiamo prefisso party davanti al nome --------
        if (party != null) {
            e.setCancelled(true); // annulliamo il comportamento standard e inviamo noi

            String partyNameRaw = party.getName(); // può contenere & per colori
            String partyPrefix = colorizeSafe("[" + partyNameRaw + "]") + ChatColor.RESET + " "; // reset evita color bleeding
            String displayName = getDisplayName(player);

            final String finalMsg = partyPrefix + displayName + ChatColor.RESET + ": " + e.getMessage();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (CommandSender r : recipients) {
                    r.sendMessage(finalMsg);
                }
                Bukkit.getConsoleSender().sendMessage("[Party] " + displayName + ": " + e.getMessage());
            });

            return;
        }

        // se non è in party non facciamo nulla -> lascia passare l'evento normale
    }
}