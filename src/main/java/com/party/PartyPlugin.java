package com.party;

import com.earth2me.essentials.Essentials;
import com.party.commands.SpyManager;
import com.party.commands.SpyCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PartyPlugin extends JavaPlugin {

    private PartyManager partyManager;
    private ChatHandler chatHandler;
    private PlayerListener playerListener;
    private SpyManager spyManager; // <-- istanza globale

    @Override
    public void onEnable() {
        this.partyManager = new PartyManager(this);
        Essentials essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
        this.spyManager = new SpyManager(); // <-- inizializzazione globale
        this.chatHandler = new ChatHandler(this, partyManager, essentials, spyManager);

        this.playerListener = new PlayerListener(partyManager, essentials);

        // Passa spyManager al PartyCommand
        PartyCommand commandExecutor = new PartyCommand(this, partyManager, chatHandler, playerListener);
        getCommand("party").setExecutor(commandExecutor);
        getCommand("party").setTabCompleter(new PartyTabCompleter(partyManager));

        getServer().getPluginManager().registerEvents(chatHandler, this);
        getServer().getPluginManager().registerEvents(playerListener, this);

        partyManager.loadParties();
        SpyManager spyManager = new SpyManager();
        SpyCommand spyCommand = new SpyCommand(spyManager);

        // Aggiorna TAB per tutti i giocatori online
        for (Player player : Bukkit.getOnlinePlayers()) {
            Party party = partyManager.getPartyByPlayer(player.getUniqueId());
            if (party != null) {
                playerListener.updatePlayerPrefix(player);
            }
        }
    }

    @Override
    public void onDisable() {
        partyManager.saveParties();
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public SpyManager getSpyManager() {
        return spyManager;
    }
}