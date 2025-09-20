package com.party;

import com.party.commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {

    private final PartyManager partyManager;
    private final ChatHandler chatHandler;
    private final PlayerListener playerListener;

    // I comandi separati
    private final CreateCommand createCommand;
    private final DescCommand descCommand;
    private final InfoCommand infoCommand;
    private final ChatCommand chatCommand;
    private final KickCommand kickCommand;
    private final PromoteCommand promoteCommand;
    private final SetLeaderCommand setLeaderCommand;
    private final NicknameCommand nicknameCommand;
    private final RankNameCommand rankNameCommand;
    private final RenameCommand renameCommand;
    private final JoinCommand joinCommand;
    private final HelpCommand helpCommand;
    private final SpyCommand spyCommand;
    private final ForceDeleteCommand forcedeleteCommand;
    private final ReloadCommand reloadCommand;
    private final VersionCommand versionCommand;

    public PartyCommand(PartyPlugin plugin, PartyManager partyManager, ChatHandler chatHandler, PlayerListener playerListener) {
        SpyManager spyManager = new SpyManager();
        this.partyManager = partyManager;
        this.chatHandler = chatHandler;
        this.playerListener = playerListener;

        // Inizializza tutti i comandi separati
        this.createCommand = new CreateCommand(partyManager, playerListener);
        this.descCommand = new DescCommand(partyManager);
        this.infoCommand = new InfoCommand(partyManager);
        this.chatCommand = new ChatCommand(partyManager);
        this.kickCommand = new KickCommand(partyManager, playerListener);
        this.promoteCommand = new PromoteCommand(partyManager, playerListener);
        this.setLeaderCommand = new SetLeaderCommand(partyManager, playerListener);
        this.nicknameCommand = new NicknameCommand(partyManager, playerListener);
        this.rankNameCommand = new RankNameCommand(partyManager, playerListener);
        this.renameCommand = new RenameCommand(partyManager, playerListener);
        this.joinCommand = new JoinCommand(partyManager, playerListener);
        this.helpCommand = new HelpCommand();
        this.spyCommand = new SpyCommand(spyManager);
        this.forcedeleteCommand = new ForceDeleteCommand(partyManager);
        this.reloadCommand = new ReloadCommand(partyManager);
        this.versionCommand = new VersionCommand(plugin);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSolo i giocatori possono usare questo comando!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsa /party help per la lista dei comandi.");
            return true;
        }

        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "create" -> createCommand.execute(player, args);
            case "desc" -> descCommand.execute(player, args);
            case "info" -> infoCommand.execute(player, args);
            case "chat" -> chatCommand.execute(player);
            case "kick" -> kickCommand.execute(player, args);
            case "promote" -> promoteCommand.execute(player, args);
            case "setleader" -> setLeaderCommand.execute(player, args);
            case "nickname" -> nicknameCommand.execute(player, args);
            case "rankname" -> rankNameCommand.execute(player, args);
            case "rename" -> renameCommand.execute(player, args);
            case "join" -> joinCommand.execute(player);
            case "help" -> helpCommand.execute(player);
            case "spy" -> spyCommand.execute(player);
            case "forcedelete" -> forcedeleteCommand.execute(player, args);
            case "reload" -> reloadCommand.execute(player);
            case "version" -> versionCommand.execute(player);
            default -> {
                player.sendMessage("§cComando sconosciuto! Usa /party help");
                yield true;
            }
        };
    }
}