package com.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map;
import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final PartyManager partyManager;
    private final ChatHandler chatHandler;
    private final PlayerListener playerListener;

    public PartyCommand(JavaPlugin plugin, PartyManager partyManager, ChatHandler chatHandler, PlayerListener playerListener) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.chatHandler = chatHandler;
        this.playerListener = playerListener;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSolo i giocatori possono eseguire questo comando.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            player.sendMessage("§eUsa /party <create|info|list|leave|chat|help>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                return handleCreate(player, uuid, args);
            case "info":
                return handleInfo(player, args);
            case "list":
                return handleList(player);
            case "leave":
                return handleLeave(player, uuid);
            case "chat":
                return handleChat(player, uuid);
            case "invite":
                return handleInvite(player, args);
            case "join":
                return handleJoin(player, uuid);
            case "kick":
                return handleKick(player, uuid, args);
            case "promote":
                return handlePromote(player, uuid, args);
            case "disband":
                return handleDisband(player, uuid);
            case "setleader":
                return handleSetLeader(player, uuid, args);
            case "rename":
                return handleRename(player, uuid, args);
            case "nickname":
                return handleNickname(player, uuid, args);
            case "help":
                return handleHelp(player);
            case "rankname":
                return handleRankName(player, uuid, args);
            default:
                player.sendMessage("§cComando sconosciuto. Usa /party help");
                return true;
        }
    }

    // -----------------------------
    // Implementazione dei comandi
    // -----------------------------

    private boolean handleCreate(Player player, UUID uuid, String[] args) {
        if (partyManager.getPartyByPlayer(uuid) != null) {
            player.sendMessage("§cSei già in un party!");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party create <nome>");
            return true;
        }
        String name = args[1];
        if (partyManager.getPartyByName(name) != null) {
            player.sendMessage("§cEsiste già un party con questo nome!");
            return true;
        }
        Party party = new Party(UUID.randomUUID().toString(), name, uuid);
        partyManager.addParty(party);
        partyManager.saveParties();
        String coloredName = ChatColor.translateAlternateColorCodes('&', name);
        player.sendMessage("§aHai creato il party " + coloredName + "§a!");
        playerListener.updatePlayerPrefix(player);
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        Party targetParty;
        if (args.length == 1) {
            targetParty = partyManager.getPartyByPlayer(player.getUniqueId());
            if (targetParty == null) {
                player.sendMessage("§cNon sei in nessun party.");
                return true;
            }
        } else {
            targetParty = partyManager.getPartyByName(args[1]);
            if (targetParty == null) {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer != null) {
                    targetParty = partyManager.getPartyByPlayer(targetPlayer.getUniqueId());
                    if (targetParty == null) {
                        player.sendMessage("§cQuel player non è in nessun party.");
                        return true;
                    }
                } else {
                    player.sendMessage("§cNessun party o player trovato con quel nome.");
                    return true;
                }
            }
        }

        String coloredName = ChatColor.translateAlternateColorCodes('&', targetParty.getName());

        player.sendMessage("§6Party: §r" + coloredName); // usa §r per resettare i colori
        player.sendMessage("§7Leader: §f" + Bukkit.getOfflinePlayer(targetParty.getLeader()).getName());
        player.sendMessage("§7Membri:");
        for (UUID memberId : targetParty.getMembers().keySet()) {
            Role role = targetParty.getMembers().get(memberId);

            String displayName = targetParty.getNicknames().get(memberId);
            if (displayName != null) {
                displayName = ChatColor.translateAlternateColorCodes('&', displayName);
            } else {
                // Se no, usa il nickname del rank (se esiste)
                String roleNick = targetParty.getRankNicknames().get(role);
                if (roleNick != null) {
                    displayName = ChatColor.translateAlternateColorCodes('&', roleNick);
                } else {
                    // fallback al nome del ruolo normale
                    displayName = role.name();
                }
            }
            player.sendMessage(" - " + Bukkit.getOfflinePlayer(memberId).getName() + " §7(" + displayName + "§7)");
        }

        return true;
    }

    private boolean handleList(Player player) {
        player.sendMessage("§6Lista party attivi:");
        for (Party p : partyManager.getAllParties()) {
            String coloredName = ChatColor.translateAlternateColorCodes('&', p.getName());
            player.sendMessage(" - " + coloredName + " §7(" + p.getMembers().size() + " membri)");
        }
        return true;
    }

    private boolean handleLeave(Player player, UUID uuid) {
        Party leaveParty = partyManager.getPartyByPlayer(uuid);
        if (leaveParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        if (leaveParty.getLeader().equals(uuid)) {
            boolean hasMods = leaveParty.getMembers().values().contains(Role.MODERATOR);
            if (!hasMods) {
                player.sendMessage("§cSei leader e non ci sono moderatori! Usa /party disband o promuovi un membro.");
                return true;
            }
            for (Map.Entry<UUID, Role> entry : leaveParty.getMembers().entrySet()) {
                if (entry.getValue() == Role.MODERATOR) {
                    leaveParty.setLeader(entry.getKey());
                    leaveParty.addMember(entry.getKey(), Role.LEADER);

                    Player newLeader = Bukkit.getPlayer(entry.getKey());
                    if (newLeader != null && newLeader.isOnline()) {
                        playerListener.updatePlayerPrefix(newLeader); // aggiorna TAB del nuovo leader
                        newLeader.sendMessage("§aSei stato promosso Leader del party!");
                    }

                    player.sendMessage("§eHai lasciato il party. Nuovo leader: " + Bukkit.getOfflinePlayer(entry.getKey()).getName());
                    if (newLeader != null) playerListener.updatePlayerPrefix(newLeader);
                    break;
                }
            }
        }

        leaveParty.removeMember(uuid);
        partyManager.updateIndexes(leaveParty);
        partyManager.saveParties();
        playerListener.updatePlayerPrefix(player);
        player.sendMessage("§aHai lasciato il party.");
        return true;
    }

    private boolean handleChat(Player player, UUID uuid) {
        Party party = partyManager.getPartyByPlayer(uuid);
        if (party == null) {
            player.sendMessage("§cNon sei in un party!");
            return true;
        }
        if (party.getChatToggled().contains(uuid)) {
            party.getChatToggled().remove(uuid);
            player.sendMessage("§7Chat party §cdisattivata§7. Torni alla chat globale.");
        } else {
            party.getChatToggled().add(uuid);
            player.sendMessage("§aChat party §aattivata§7. Ora scriverai solo ai membri del party.");
        }
        return true;
    }
    private boolean handleInvite(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsa: /party invite <player>");
            return true;
        }

        Party party = partyManager.getPartyByPlayer(sender.getUniqueId());
        if (party == null) {
            sender.sendMessage("§cNon sei in nessun party.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cQuel giocatore non è online.");
            return true;
        }

        if (partyManager.isInParty(target.getUniqueId())) {
            sender.sendMessage("§cQuel giocatore è già in un party!");
            return true;
        }

        // Aggiungi invito
        partyManager.addInvite(target.getUniqueId(), party);
        sender.sendMessage("§aHai invitato §e" + target.getName() + "§a nel party!");
        target.sendMessage("§eSei stato invitato nel party §b" + party.getName() +
                "§e da §a" + sender.getName() + "§e. Hai 30 secondi per accettare con §a/party join");
        return true;
    }
    private boolean handleJoin(Player player, UUID uuid) {
        if (partyManager.isInParty(uuid)) {
            player.sendMessage("§cSei già in un party!");
            return true;
        }

        Party party = partyManager.getValidInvite(uuid);
        if (party == null) {
            player.sendMessage("§cNon hai inviti validi o l'invito è scaduto.");
            return true;
        }

        partyManager.addMember(party, uuid, Role.MEMBER);
        partyManager.removeInvite(uuid);

        player.sendMessage("§aSei entrato nel party §b" + party.getName());
        for (UUID memberId : party.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.sendMessage("§e" + player.getName() + " §aè entrato nel party!");
            }
        }
        return true;
    }
    private boolean handleKick(Player player, UUID uuid, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party kick <player>");
            return true;
        }

        Party kickParty = partyManager.getPartyByPlayer(uuid);
        if (kickParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer non trovato online!");
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        if (!kickParty.getMembers().containsKey(targetUUID)) {
            player.sendMessage("§cQuel giocatore non è nel tuo party!");
            return true;
        }
        if (kickParty.getLeader().equals(targetUUID)) {
            player.sendMessage("§cNon puoi kickare il Leader!");
            return true;
        }

        Role myRole = kickParty.getMembers().get(uuid);
        Role targetRole = kickParty.getMembers().get(targetUUID);
        if (myRole.getPower() <= targetRole.getPower()) {
            player.sendMessage("§cNon hai permessi sufficienti per kickare questo membro!");
            return true;
        }

        kickParty.removeMember(targetUUID);
        partyManager.updateIndexes(kickParty);
        partyManager.saveParties();
        playerListener.updatePlayerPrefix(target);
        target.sendMessage("§cSei stato kickato dal party da §e" + player.getDisplayName());
        player.sendMessage("§aHai kickato §e" + target.getName());
        return true;
    }

    private boolean handleDisband(Player player, UUID uuid) {
        Party disbandParty = partyManager.getPartyByPlayer(uuid);
        if (disbandParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }
        if (!disbandParty.getLeader().equals(uuid)) {
            player.sendMessage("§cSolo il Leader può sciogliere il party.");
            return true;
        }
        for (UUID memberId : disbandParty.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                playerListener.updatePlayerPrefix(member); // reset TAB
                if (!memberId.equals(uuid)) member.sendMessage("§cIl party è stato sciolto dal Leader.");
            }
        }
        partyManager.removeParty(disbandParty.getId());
        partyManager.saveParties();
        // Reset anche del Leader
        playerListener.updatePlayerPrefix(player);
        player.sendMessage("§cHai sciolto il tuo party.");
        return true;
    }

    private boolean handleSetLeader(Player player, UUID uuid, String[] args) {
        Party setLeaderParty = partyManager.getPartyByPlayer(uuid);
        if (setLeaderParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }
        if (!setLeaderParty.getLeader().equals(uuid)) {
            player.sendMessage("§cSolo il Leader può usare questo comando.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party setleader <player>");
            return true;
        }
        Player newLeaderPlayer = Bukkit.getPlayer(args[1]);
        if (newLeaderPlayer == null) {
            player.sendMessage("§cPlayer non trovato online!");
            return true;
        }
        UUID newLeaderUUID = newLeaderPlayer.getUniqueId();
        if (!setLeaderParty.getMembers().containsKey(newLeaderUUID)) {
            player.sendMessage("§cQuel giocatore non è nel tuo party!");
            return true;
        }
        setLeaderParty.setLeader(newLeaderUUID);
        setLeaderParty.addMember(newLeaderUUID, Role.LEADER);
        setLeaderParty.addMember(uuid, Role.MODERATOR); // retrocede l'ex leader
        playerListener.updatePlayerPrefix(newLeaderPlayer);
        playerListener.updatePlayerPrefix(player);
        partyManager.saveParties();
        player.sendMessage("§aHai nominato §e" + newLeaderPlayer.getDisplayName() + "§a come nuovo Leader.");
        return true;
    }

    private boolean handleRename(Player player, UUID uuid, String[] args) {
        Party renameParty = partyManager.getPartyByPlayer(uuid);
        if (renameParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }
        if (!renameParty.getLeader().equals(uuid)) {
            player.sendMessage("§cSolo il Leader può rinominare il party.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party rename <nuovoNome>");
            return true;
        }
        String newName = args[1];
        if (partyManager.getPartyByName(newName) != null) {
            player.sendMessage("§cEsiste già un party con questo nome.");
            return true;
        }
        renameParty.setName(newName);
        for (UUID memberId : renameParty.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) playerListener.updatePlayerPrefix(member);
        }
        String coloredName = ChatColor.translateAlternateColorCodes('&', newName);
        player.sendMessage("§aHai rinominato il party in " + coloredName + "§r");
        return true;
    }

    private boolean handleNickname(Player player, UUID uuid, String[] args) {
        Party nickParty = partyManager.getPartyByPlayer(uuid);
        if (nickParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }
        Role myRole = nickParty.getMembers().get(uuid);
        if (myRole != Role.LEADER && myRole != Role.MODERATOR) {
            player.sendMessage("§cNon hai i permessi per usare questo comando.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party nickname <player> [nickname]");
            return true;
        }
        Player targetNickPlayer = Bukkit.getPlayer(args[1]);
        if (targetNickPlayer == null) {
            player.sendMessage("§cPlayer non trovato online!");
            return true;
        }
        UUID targetNickUUID = targetNickPlayer.getUniqueId();
        if (!nickParty.getMembers().containsKey(targetNickUUID)) {
            player.sendMessage("§cQuel giocatore non è nel tuo party!");
            return true;
        }
        if (args.length == 2) {
            nickParty.getNicknames().remove(targetNickUUID);
            playerListener.updatePlayerPrefix(targetNickPlayer);
            player.sendMessage("§aNickname resettato per " + targetNickPlayer.getDisplayName());
        } else {
            String nick = args[2];
            if (Role.getDefaultNames().contains(nick.toLowerCase())) {
                player.sendMessage("§cNon puoi usare un nome uguale a un rank predefinito.");
                return true;
            }
            nickParty.getNicknames().put(targetNickUUID, nick);
            playerListener.updatePlayerPrefix(targetNickPlayer);
            partyManager.saveParties();
            String coloredNick = ChatColor.translateAlternateColorCodes('&', nick);
            player.sendMessage("§aImpostato nickname ruolo per " + targetNickPlayer.getDisplayName() + ": §e" + coloredNick);
        }
        return true;
    }

    private boolean handleRankName(Player player, UUID uuid, String[] args) {
        Party rankParty = partyManager.getPartyByPlayer(uuid);
        if (rankParty == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }
        if (!rankParty.getLeader().equals(uuid)) {
            player.sendMessage("§cSolo il Leader può cambiare i nomi dei rank.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party rankname <rank> [nickname]");
            return true;
        }
        Role roleTarget = Role.fromString(args[1]);
        if (roleTarget == null) {
            player.sendMessage("§cRank non valido. Usa: MEMBER, MODERATOR, LEADER");
            return true;
        }
        if (args.length == 2) {
            rankParty.getRankNicknames().remove(roleTarget);
            for (UUID memberId : rankParty.getMembers().keySet()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) playerListener.updatePlayerPrefix(member);
            }
            player.sendMessage("§aNickname rank resettato per " + roleTarget.name());
        } else {
            String nick = args[2];
            if (Role.getDefaultNames().contains(nick.toLowerCase())) {
                player.sendMessage("§cNon puoi usare un nome uguale a un rank predefinito.");
                return true;
            }
            rankParty.getRankNicknames().put(roleTarget, nick);
            for (UUID memberId : rankParty.getMembers().keySet()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) playerListener.updatePlayerPrefix(member);
            }
            partyManager.saveParties();
            String coloredRank = ChatColor.translateAlternateColorCodes('&', nick);
            player.sendMessage("§aImpostato nickname per il rank " + roleTarget.name() + ": §e" + coloredRank);
        }
        return true;
    }

    private boolean handlePromote(Player player, UUID uuid, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsa: /party promote <player>");
            return true;
        }

        Party party = partyManager.getPartyByPlayer(uuid);
        if (party == null) {
            player.sendMessage("§cNon sei in nessun party!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer non trovato online!");
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        if (!party.getMembers().containsKey(targetUUID)) {
            player.sendMessage("§cQuel giocatore non è nel tuo party!");
            return true;
        }
        if (party.getLeader().equals(targetUUID)) {
            player.sendMessage("§cNon puoi promuovere il Leader.");
            return true;
        }

        party.addMember(targetUUID, Role.MODERATOR);
        playerListener.updatePlayerPrefix(target);
        partyManager.saveParties();
        player.sendMessage("§aHai promosso §e" + target.getName() + " a Moderatore.");
        return true;
    }
    private boolean handleHelp(Player player) {
        player.sendMessage("§6====== Comandi Party ======");
        player.sendMessage("§e/party create <nome> §7- Crea un nuovo party");
        player.sendMessage("§e/party disband §7- Scioglie il tuo party (solo leader)");
        player.sendMessage("§e/party info [giocatore|party] §7- Mostra info sul party");
        player.sendMessage("§e/party list §7- Lista di tutti i party");
        player.sendMessage("§e/party invite <giocatore> §7- Invita un giocatore");
        player.sendMessage("§e/party kick <giocatore> §7- Rimuove un giocatore");
        player.sendMessage("§e/party promote <giocatore> <rank> §7- Promuove un giocatore");
        player.sendMessage("§e/party setleader <giocatore> §7- Imposta un nuovo leader");
        player.sendMessage("§e/party leave §7- Esci dal party");
        player.sendMessage("§e/party chat §7- Attiva/disattiva la chat party");
        player.sendMessage("§e/party rename <nome> §7- Rinomina il tuo party");
        player.sendMessage("§e/party nickname <giocatore> <nickname> §7- Imposta un nickname personalizzato");
        player.sendMessage("§e/party rankname <rank> <nome> §7- Rinomina un ruolo");
        player.sendMessage("§6===========================");
        return true;
    }
    // ---------- TODO: disband, setleader, rename, nickname, rankname ----------
    // Per semplicità il modello è lo stesso: modifica party, poi chiama
    // playerListener.updatePlayerPrefix(player) o target per aggiornare TAB
}