package com.valorantmc.guns;

import org.bukkit.Material;
import org.bukkit.Sound;

/**
 * Valorant-inspired guns mapped to Minecraft items.
 * Each gun has damage, fire rate (ticks between shots), magazine size, price, and reload time.
 */
public enum Gun {
    //         Name         Material              Damage  FireRate  Mag  Price  Reload
    CLASSIC   ("Classic",   Material.WOODEN_HOW,  45,     8,        12,  0,     25),
    SHORTY    ("Shorty",    Material.STONE_HOW,   70,     15,       2,   200,   20),
    SHERIFF   ("Sheriff",   Material.CROSSBOW,    145,    25,       6,   800,   35),
    PHANTOM   ("Phantom",   Material.BOW,         35,     4,        30,  2900,  30),
    VANDAL    ("Vandal",    Material.GOLDEN_HOW,  40,     4,        25,  2900,  30),
    OPERATOR  ("Operator",  Material.DIAMOND_HOW, 255,    40,       5,   4700,  45),
    SPECTRE   ("Spectre",   Material.BOW,         26,     3,        30,  1600,  22),
    BUCKY     ("Bucky",     Material.STONE_SWORD, 60,     18,       5,   850,   30);

    private final String displayName;
    private final Material material;
    private final int damage;
    private final int fireRateTicks;
    private final int magazineSize;
    private final int price;
    private final int reloadTicks;

    Gun(String displayName, Material material, int damage,
        int fireRateTicks, int magazineSize, int price, int reloadTicks) {
        this.displayName = displayName;
        this.material = material;
        this.damage = damage;
        this.fireRateTicks = fireRateTicks;
        this.magazineSize = magazineSize;
        this.price = price;
        this.reloadTicks = reloadTicks;
    }

    public String getDisplayName() { return displayName; }
    public Material getMaterial()  { return material; }
    public int getDamage()         { return damage; }
    public int getFireRateTicks()  { return fireRateTicks; }
    public int getMagazineSize()   { return magazineSize; }
    public int getPrice()          { return price; }
    public int getReloadTicks()    { return reloadTicks; }

    public static Gun fromMaterial(Material mat) {
        for (Gun g : values()) {
            if (g.material == mat) return g;
        }
        return null;
    }

    public static Gun fromName(String name) {
        for (Gun g : values()) {
            if (g.displayName.equalsIgnoreCase(name) || g.name().equalsIgnoreCase(name)) return g;
        }
        return null;
    }
}
