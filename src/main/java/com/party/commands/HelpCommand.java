package com.party.commands;

import org.bukkit.entity.Player;

public class HelpCommand {

    public boolean execute(Player player) {
        player.sendMessage("§6--- Comandi Party ---");
        player.sendMessage("§e/party create <nome> §7- Crea un party");
        player.sendMessage("§e/party info [player/party] §7- Mostra info party");
        player.sendMessage("§e/party invite <player> §7- Invita un giocatore");
        player.sendMessage("§e/party join §7- Entra in un party se invitato");
        player.sendMessage("§e/party kick <player> §7- Espelli un membro");
        player.sendMessage("§e/party promote <player> <rank> §7- Promuovi un membro");
        player.sendMessage("§e/party setleader <player> §7- Imposta nuovo leader");
        player.sendMessage("§e/party nickname <player> <nickname> §7- Imposta nickname per il rank del giocatore");
        player.sendMessage("§e/party rankname <rank> <nome> §7- Personalizza nome rank");
        player.sendMessage("§e/party rename <nome> §7- Rinomina il party");
        player.sendMessage("§e/party help §7- Mostra questo messaggio");
        return true;
    }
}
