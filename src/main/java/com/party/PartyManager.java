package com.party;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PartyManager {
    private final JavaPlugin plugin;

    // Tutti i party attivi: id -> Party
    private final Map<String, Party> parties = new HashMap<>();

    // Index rapido: player -> partyId
    private final Map<UUID, String> playerToParty = new HashMap<>();

    // File YAML per persistenza
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, InviteEntry> invites = new HashMap<>();

    // --- COSTRUTTORE ---
    public PartyManager(JavaPlugin plugin) {
        this.plugin = plugin;

        this.file = new File(plugin.getDataFolder(), "parties.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    // --- CRUD ---
    public void loadParties() {
        if (!file.exists()) return;

        for (String key : config.getKeys(false)) {
            String name = config.getString(key + ".name");
            UUID leader = UUID.fromString(config.getString(key + ".leader"));
            Party party = new Party(key, name, leader);
            party.setDescription(config.getString(key + ".description", ""));

            // Membri
            if (config.contains(key + ".members")) {
                for (String memberId : config.getConfigurationSection(key + ".members").getKeys(false)) {
                    UUID memberUUID = UUID.fromString(memberId);
                    Role role = Role.valueOf(config.getString(key + ".members." + memberId));
                    party.addMember(memberUUID, role);
                }
            }

            // Rank nickname
            if (config.contains(key + ".rankNicknames")) {
                for (String roleKey : config.getConfigurationSection(key + ".rankNicknames").getKeys(false)) {
                    Role rt = Role.valueOf(roleKey);
                    party.getRankNicknames().put(rt, config.getString(key + ".rankNicknames." + roleKey));
                }
            }

            parties.put(key, party);
            updateIndexes(party); // aggiorna playerToParty
        }
    }

    public void saveParties() {
        for (Party party : parties.values()) {
            config.set(party.getId() + ".name", party.getName());
            config.set(party.getId() + ".leader", party.getLeader().toString());

            for (Map.Entry<UUID, Role> entry : party.getMembers().entrySet()) {
                config.set(party.getId() + ".members." + entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<Role, String> entry : party.getRankNicknames().entrySet()) {
                config.set(party.getId() + ".rankNicknames." + entry.getKey(), entry.getValue());
            }

            // Salva la descrizione del party
            config.set(party.getId() + ".description", party.getDescription());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addParty(Party party) {
        parties.put(party.getId(), party);
        for (UUID member : party.getMembers().keySet()) {
            playerToParty.put(member, party.getId());
        }
    }

    public void removeParty(String id) {
        Party party = parties.remove(id);
        if (party != null) {
            for (UUID member : party.getMembers().keySet()) {
                playerToParty.remove(member);
            }
        }
    }

    public Party getPartyById(String id) {
        return parties.get(id);
    }

    public Party getPartyByName(String name) {
        for (Party party : parties.values()) {
            if (party.getName().equalsIgnoreCase(name)) {
                return party;
            }
        }
        return null;
    }

    public Party getPartyByPlayer(UUID player) {
        String id = playerToParty.get(player);
        if (id == null) return null;
        return parties.get(id);
    }

    public Collection<Party> getAllParties() {
        return parties.values();
    }

    // --- Gestione membri ---
    public void addMember(Party party, UUID player, Role role) {
        party.addMember(player, role);
        playerToParty.put(player, party.getId());
    }
    public void disbandParty(Party party) {
        if (party == null) return;

        // Rimuovi tutti i giocatori da quel party
        for (UUID member : new HashSet<>(party.getMembers().keySet())) {
            party.removeMember(member);
        }

        // Rimuovi il party dalla lista globale
        parties.remove(party.getName().toLowerCase());
    }
    public void removeMember(Party party, UUID player) {
        party.removeMember(player);
        playerToParty.remove(player);
        if (party.getMembers().isEmpty()) {
            removeParty(party.getId());
        }
    }

    public boolean isInParty(UUID player) {
        return playerToParty.containsKey(player);
    }

    public void updateIndexes(Party party) {
        for (UUID member : party.getMembers().keySet()) {
            playerToParty.put(member, party.getId());
        }
    }
    private static class InviteEntry {
        private final Party party;
        private final long expireAt;

        public InviteEntry(Party party, long expireAt) {
            this.party = party;
            this.expireAt = expireAt;
        }

        public Party getParty() {
            return party;
        }

        public boolean isValid() {
            return System.currentTimeMillis() <= expireAt;
        }
    }
    public void addInvite(UUID invited, Party party) {
        invites.put(invited, new InviteEntry(party, System.currentTimeMillis() + 30_000)); // 30 secondi
    }

    public Party getValidInvite(UUID invited) {
        InviteEntry entry = invites.get(invited);
        if (entry != null && entry.isValid()) {
            return entry.getParty();
        }
        invites.remove(invited); // pulizia se scaduto
        return null;
    }

    public void removeInvite(UUID invited) {
        invites.remove(invited);
    }
}