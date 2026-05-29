package com.valorantmc.agents;

/**
 * 5 Valorant agents with their abilities.
 * Each agent has a passive description and 2 active abilities (Q and E).
 */
public enum Agent {

    JETT(
        "Jett",
        "§bJett",
        "§7Fast and evasive. Low gravity jumps.",
        "§e[Q] Updraft §7- Launch yourself upward (double jump).",
        "§e[E] Tailwind §7- Dash forward instantly.",
        20, // ability Q cooldown seconds
        15  // ability E cooldown seconds
    ),

    SAGE(
        "Sage",
        "§aSage",
        "§7Healer. Gains slow orbs to block paths.",
        "§a[Q] Slow Orb §7- Throw a slow orb that creates a slowing zone.",
        "§a[E] Healing Orb §7- Heal yourself or a nearby ally.",
        18,
        25
    ),

    PHOENIX(
        "Phoenix",
        "§6Phoenix",
        "§7Fire-powered duelist. Heals on kills.",
        "§6[Q] Curveball §7- Throw a flare that blinds nearby enemies.",
        "§6[E] Blaze §7- Summon a wall of fire that blocks vision.",
        15,
        20
    ),

    REYNA(
        "Reyna",
        "§5Reyna",
        "§7Vampire duelist. Absorbs soul orbs after kills.",
        "§5[Q] Devour §7- Consume soul orb to heal rapidly.",
        "§5[E] Dismiss §7- Consume soul orb to become intangible briefly.",
        12,
        18
    ),

    BREACH(
        "Breach",
        "§cBreach",
        "§7Initiator. Disrupts enemies through walls.",
        "§c[Q] Flashpoint §7- Blind enemies through a wall.",
        "§c[E] Fault Line §7- Send a seismic blast that disorients foes.",
        20,
        22
    );

    private final String id;
    private final String displayName;
    private final String passive;
    private final String abilityQ;
    private final String abilityE;
    private final int qCooldown;
    private final int eCooldown;

    Agent(String id, String displayName, String passive,
          String abilityQ, String abilityE, int qCooldown, int eCooldown) {
        this.id = id;
        this.displayName = displayName;
        this.passive = passive;
        this.abilityQ = abilityQ;
        this.abilityE = abilityE;
        this.qCooldown = qCooldown;
        this.eCooldown = eCooldown;
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public String getPassive()     { return passive; }
    public String getAbilityQ()    { return abilityQ; }
    public String getAbilityE()    { return abilityE; }
    public int getQCooldown()      { return qCooldown; }
    public int getECooldown()      { return eCooldown; }

    public static Agent fromName(String name) {
        for (Agent a : values()) {
            if (a.id.equalsIgnoreCase(name) || a.name().equalsIgnoreCase(name)) return a;
        }
        return null;
    }
}
