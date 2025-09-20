package com.party;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PartyTabCompleter implements TabCompleter {

    private final PartyManager partyManager;

    public PartyTabCompleter(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        Party party = partyManager.getPartyByPlayer(uuid);

        List<String> completions = new ArrayList<>();

        // Lista dei subcomandi generali
        List<String> allCommands = Arrays.asList(
                "create", "info", "list", "leave", "chat",
                "kick", "promote", "disband", "setleader", "rename",
                "nickname", "rankname"
        );

        if (args.length == 1) {
            // Tab completamento primo argomento: subcomando
            for (String cmd : allCommands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
            return completions;
        }

        String sub = args[0].toLowerCase();

        // Completamento secondo argomento
        switch (sub) {
            case "info":
            case "kick":
            case "promote":
                if (args.length == 2) {
                    // Completamento del secondo argomento: nome del player
                    for (Player p : player.getServer().getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(p.getName());
                        }
                    }
                } else if (args.length == 3) {
                    // Completamento del terzo argomento: ruolo
                    for (Role role : Role.values()) {
                        if (role.name().toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(role.name());
                        }
                    }
                }
                break;
            case "setleader":
            case "nickname":
                // Suggerisci solo nomi dei giocatori online
                for (Player p : player.getServer().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                }
                break;

            case "create":
            case "rename":
                // Possibili nomi di party (vuoto o suggerimenti personalizzati)
                break;

            case "rankname":
                // Suggerisci i ruoli standard
                for (Role role : Role.values()) {
                    if (role.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(role.name());
                    }
                }
                break;
        }

        return completions;
    }
}