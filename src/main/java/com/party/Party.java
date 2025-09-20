package com.party;

import java.util.*;

public class Party {

    private final String id; // ID univoco (UUID string)
    private String name; // Nome del party
    private UUID leader; // UUID del leader

    // Mappa membri e loro ruoli
    private final Map<UUID, Role> members = new HashMap<>();

    // Nick personalizzati dei ruoli (solo estetici, validi solo in questo party)
    private final Map<Role, String> rankNicknames = new HashMap<>();

    // Nickname personalizzato del rank per singolo player
    private final Map<UUID, String> nicknames = new HashMap<>();

    // Giocatori con la chat party attiva
    private final Set<UUID> chatToggled = new HashSet<>();

    private String description = "";

    public Party(String id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.members.put(leader, Role.LEADER);
    }

    // --- GETTER & SETTER ---
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Map<UUID, Role> getMembers() {
        return members;
    }

    public Map<Role, String> getRankNicknames() {
        return rankNicknames;
    }

    public Map<UUID, String> getNicknames() {
        return nicknames;
    }

    public Set<UUID> getChatToggled() {
        return chatToggled;
    }

    // --- METODI MEMBRI ---
    public void addMember(UUID uuid, Role role) {
        members.put(uuid, role);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        chatToggled.remove(uuid);
        nicknames.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public Role getRole(UUID uuid) {
        return members.get(uuid);
    }

    // --- NICKNAME UTILITY ---
    public String getDisplayRole(UUID uuid) {
        Role role = members.get(uuid);
        if (role == null) return "N/A";

        // Nickname specifico del player
        if (nicknames.containsKey(uuid)) {
            return nicknames.get(uuid);
        }

        // Nickname del rank, se esiste
        if (rankNicknames.containsKey(role)) {
            return rankNicknames.get(role);
        }

        // Default
        return role.getDefaultName();
    }
}