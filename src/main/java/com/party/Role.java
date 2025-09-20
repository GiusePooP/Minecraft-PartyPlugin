package com.party;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Role {
    MEMBER(0),
    MODERATOR(1),
    LEADER(2);

    private final int power;

    Role(int power) {
        this.power = power;
    }

    public int getPower() {
        return power;
    }

    // --- Metodo aggiunto ---
    public String getDefaultName() {
        return this.name().substring(0, 1) + this.name().substring(1).toLowerCase();
        // Restituisce "Member", "Moderator", "Leader"
    }

    public static Role fromString(String input) {
        try {
            return Role.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Set<String> getDefaultNames() {
        return new HashSet<>(Arrays.asList("member", "moderator", "leader"));
    }
}