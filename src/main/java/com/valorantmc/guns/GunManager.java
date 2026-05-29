package com.valorantmc.guns;

import com.valorantmc.ValorantPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GunManager {

    private final ValorantPlugin plugin;
    // playerId -> Gun
    private final Map<UUID, Gun> equippedGuns = new HashMap<>();
    // playerId -> current ammo
    private final Map<UUID, Integer> ammo = new HashMap<>();
    // playerId -> last shot tick (cooldown)
    private final Map<UUID, Long> lastShot = new HashMap<>();
    // playerId -> reload task
    private final Map<UUID, BukkitTask> reloadTasks = new HashMap<>();
    // playerId -> is reloading
    private final Map<UUID, Boolean> reloading = new HashMap<>();

    public GunManager(ValorantPlugin plugin) {
        this.plugin = plugin;
    }

    public void giveGun(Player player, Gun gun) {
        equippedGuns.put(player.getUniqueId(), gun);
        ammo.put(player.getUniqueId(), gun.getMagazineSize());
        reloading.put(player.getUniqueId(), false);

        ItemStack item = createGunItem(gun, gun.getMagazineSize());
        player.getInventory().setItem(0, item);
        player.sendMessage(ChatColor.GOLD + "» " + ChatColor.WHITE + "Equipped " +
                ChatColor.YELLOW + gun.getDisplayName() + ChatColor.GRAY +
                " (" + gun.getMagazineSize() + "/" + gun.getMagazineSize() + ")");
    }

    public ItemStack createGunItem(Gun gun, int currentAmmo) {
        ItemStack item = new ItemStack(gun.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "✦ " + gun.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Damage: " + ChatColor.RED + gun.getDamage());
            lore.add(ChatColor.GRAY + "Ammo:   " + ChatColor.WHITE + currentAmmo + "/" + gun.getMagazineSize());
            lore.add(ChatColor.GRAY + "Price:  " + ChatColor.GOLD + gun.getPrice() + "¢");
            lore.add(ChatColor.DARK_GRAY + "» Right-click to shoot");
            lore.add(ChatColor.DARK_GRAY + "» Sneak+Right to reload");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /** Returns true if shot was fired, false if blocked (cooldown / reloading / no ammo) */
    public boolean tryShoot(Player player) {
        UUID id = player.getUniqueId();
        Gun gun = equippedGuns.get(id);
        if (gun == null) return false;
        if (Boolean.TRUE.equals(reloading.get(id))) {
            player.sendActionBar(ChatColor.RED + "Reloading...");
            return false;
        }

        long now = System.currentTimeMillis();
        long last = lastShot.getOrDefault(id, 0L);
        long cooldownMs = gun.getFireRateTicks() * 50L;
        if (now - last < cooldownMs) return false;

        int currentAmmo = ammo.getOrDefault(id, 0);
        if (currentAmmo <= 0) {
            player.sendActionBar(ChatColor.RED + "▐ EMPTY - Reload! (Sneak+RClick)");
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1f, 1.2f);
            return false;
        }

        ammo.put(id, currentAmmo - 1);
        lastShot.put(id, now);
        updateGunItem(player, gun);
        playShootSound(player, gun);
        return true;
    }

    public void startReload(Player player) {
        UUID id = player.getUniqueId();
        Gun gun = equippedGuns.get(id);
        if (gun == null) return;
        if (Boolean.TRUE.equals(reloading.get(id))) return;
        if (ammo.getOrDefault(id, 0) >= gun.getMagazineSize()) {
            player.sendActionBar(ChatColor.GREEN + "Magazine full!");
            return;
        }

        reloading.put(id, true);
        player.sendActionBar(ChatColor.YELLOW + "Reloading " + gun.getDisplayName() + "...");
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1f, 1.5f);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            ammo.put(id, gun.getMagazineSize());
            reloading.put(id, false);
            updateGunItem(player, gun);
            player.sendActionBar(ChatColor.GREEN + "✔ Reloaded!");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.4f);
        }, gun.getReloadTicks());

        if (reloadTasks.containsKey(id)) reloadTasks.get(id).cancel();
        reloadTasks.put(id, task);
    }

    private void updateGunItem(Player player, Gun gun) {
        int currentAmmo = ammo.getOrDefault(player.getUniqueId(), 0);
        ItemStack item = createGunItem(gun, currentAmmo);
        player.getInventory().setItem(0, item);
    }

    private void playShootSound(Player player, Gun gun) {
        Sound sound;
        float pitch;
        switch (gun) {
            case OPERATOR: sound = Sound.ENTITY_FIREWORK_ROCKET_LAUNCH; pitch = 0.7f; break;
            case SHERIFF:  sound = Sound.ENTITY_GENERIC_EXPLODE;        pitch = 1.5f; break;
            case SHORTY:   sound = Sound.ENTITY_GENERIC_EXPLODE;        pitch = 0.8f; break;
            case PHANTOM:
            case VANDAL:   sound = Sound.ENTITY_ARROW_SHOOT;            pitch = 1.1f; break;
            default:       sound = Sound.ENTITY_ARROW_SHOOT;            pitch = 1.0f; break;
        }
        player.getWorld().playSound(player.getLocation(), sound, 1f, pitch);
    }

    public Gun getEquippedGun(Player player) {
        return equippedGuns.get(player.getUniqueId());
    }

    public void clearPlayer(Player player) {
        UUID id = player.getUniqueId();
        equippedGuns.remove(id);
        ammo.remove(id);
        lastShot.remove(id);
        reloading.remove(id);
        if (reloadTasks.containsKey(id)) {
            reloadTasks.get(id).cancel();
            reloadTasks.remove(id);
        }
    }

    public void clearAll() {
        reloadTasks.values().forEach(BukkitTask::cancel);
        reloadTasks.clear();
        equippedGuns.clear();
        ammo.clear();
        lastShot.clear();
        reloading.clear();
    }
}
