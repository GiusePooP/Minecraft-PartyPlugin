package com.party.commands;

import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpyManager {

    private final Set<UUID> activeSpies = new HashSet<>();

    public void enableSpy(Player player) {
        activeSpies.add(player.getUniqueId());
    }

    public void disableSpy(Player player) {
        activeSpies.remove(player.getUniqueId());
    }

    public boolean isSpying(Player player) {
        return activeSpies.contains(player.getUniqueId());
    }

    public void sendSpyMessage(Player sender, String message) {
        for (UUID spyUUID : activeSpies) {
            Player spy = sender.getServer().getPlayer(spyUUID);
            if (spy != null && spy.isOnline() && !spy.equals(sender)) {
                spy.sendMessage("§8[Spy] " + message);
            }
        }
    }
}