package com.valorantmc.agents;

import com.valorantmc.ValorantPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class AgentManager {

    private final ValorantPlugin plugin;
    private final Map<UUID, Agent>  selectedAgents  = new HashMap<>();
    private final Map<UUID, Long>   qCooldowns      = new HashMap<>();
    private final Map<UUID, Long>   eCooldowns      = new HashMap<>();
    // Reyna: soul orbs available after kill
    private final Map<UUID, Integer> soulOrbs        = new HashMap<>();

    public AgentManager(ValorantPlugin plugin) {
        this.plugin = plugin;
    }

    public void selectAgent(Player player, Agent agent) {
        selectedAgents.put(player.getUniqueId(), agent);
        qCooldowns.remove(player.getUniqueId());
        eCooldowns.remove(player.getUniqueId());
        applyPassive(player, agent);
        player.sendMessage("");
        player.sendMessage(agent.getDisplayName() + " §7selected!");
        player.sendMessage(agent.getPassive());
        player.sendMessage(agent.getAbilityQ());
        player.sendMessage(agent.getAbilityE());
        player.sendMessage("");
    }

    // ─────────────────── Ability Q ───────────────────
    public void useAbilityQ(Player player) {
        Agent agent = selectedAgents.get(player.getUniqueId());
        if (agent == null) { player.sendMessage("§cSelect an agent first! /vagent <name>"); return; }
        if (!checkCooldown(player, agent, true)) return;

        switch (agent) {
            case JETT:    jettUpdraft(player);  break;
            case SAGE:    sageSlowOrb(player);  break;
            case PHOENIX: phoenixCurveball(player); break;
            case REYNA:   reynaDevour(player);  break;
            case BREACH:  breachFlashpoint(player); break;
        }
        setCooldown(player, agent, true);
    }

    // ─────────────────── Ability E ───────────────────
    public void useAbilityE(Player player) {
        Agent agent = selectedAgents.get(player.getUniqueId());
        if (agent == null) { player.sendMessage("§cSelect an agent first! /vagent <name>"); return; }
        if (!checkCooldown(player, agent, false)) return;

        switch (agent) {
            case JETT:    jettTailwind(player);  break;
            case SAGE:    sageHeal(player);       break;
            case PHOENIX: phoenixBlaze(player);   break;
            case REYNA:   reynaDismiss(player);   break;
            case BREACH:  breachFaultLine(player); break;
        }
        setCooldown(player, agent, false);
    }

    // ─────────── JETT ───────────
    private void jettUpdraft(Player p) {
        p.setVelocity(p.getVelocity().setY(1.4));
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 20, 0.5, 0.2, 0.5, 0.05);
        p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.3f);
        p.sendActionBar("§b✦ Updraft!");
    }

    private void jettTailwind(Player p) {
        Vector dir = p.getLocation().getDirection().setY(0).normalize().multiply(1.8);
        p.setVelocity(dir);
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, p.getLocation(), 10, 0.3, 0.3, 0.3, 0.1);
        p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 0.5f, 2.0f);
        p.sendActionBar("§b✦ Tailwind!");
    }

    // ─────────── SAGE ───────────
    private void sageSlowOrb(Player p) {
        Location target = p.getLocation().add(p.getLocation().getDirection().multiply(6));
        p.getWorld().spawnParticle(Particle.SNOWBALL, target, 60, 1.5, 0.5, 1.5, 0.05);
        p.getWorld().playSound(target, Sound.BLOCK_SNOW_PLACE, 1f, 0.5f);
        // Apply slowness to nearby enemies
        for (Entity e : p.getNearbyEntities(5, 3, 5)) {
            if (e instanceof Player enemy && !enemy.equals(p)) {
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 3));
                enemy.sendActionBar("§7❄ Sage Slow Orb!");
            }
        }
        p.sendActionBar("§a✦ Slow Orb thrown!");
    }

    private void sageHeal(Player p) {
        // Heal self or nearest ally
        Player target = p;
        double closest = 8;
        for (Entity e : p.getNearbyEntities(8, 5, 8)) {
            if (e instanceof Player ally && !ally.equals(p)) {
                double dist = ally.getLocation().distance(p.getLocation());
                if (dist < closest) { closest = dist; target = ally; }
            }
        }
        double healAmount = Math.min(target.getMaxHealth() - target.getHealth(), 8);
        target.setHealth(target.getHealth() + healAmount);
        target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0,1,0), 8, 0.5, 0.5, 0.5, 0.05);
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f);
        target.sendActionBar("§a✦ Sage Heal!");
        if (!target.equals(p)) p.sendActionBar("§a✦ Healing " + target.getName() + "!");
    }

    // ─────────── PHOENIX ───────────
    private void phoenixCurveball(Player p) {
        Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(4));
        p.getWorld().spawnParticle(Particle.FLAME, loc, 40, 0.5, 0.5, 0.5, 0.05);
        p.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1f, 1.5f);
        for (Entity e : p.getNearbyEntities(6, 4, 6)) {
            if (e instanceof Player enemy && !enemy.equals(p)) {
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                enemy.sendActionBar("§6⚡ Blinded by Phoenix Curveball!");
            }
        }
        p.sendActionBar("§6✦ Curveball!");
    }

    private void phoenixBlaze(Player p) {
        Location loc = p.getLocation();
        for (int i = -3; i <= 3; i++) {
            Location fireLoc = loc.clone().add(p.getLocation().getDirection().multiply(i)).add(0, 0, 0);
            fireLoc.getBlock().setType(Material.FIRE);
        }
        p.playSound(loc, Sound.ENTITY_BLAZE_AMBIENT, 1f, 0.8f);
        p.sendActionBar("§6✦ Blaze wall!");
        // Remove fire after 5 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int i = -3; i <= 3; i++) {
                Location fireLoc = loc.clone().add(p.getLocation().getDirection().multiply(i));
                if (fireLoc.getBlock().getType() == Material.FIRE)
                    fireLoc.getBlock().setType(Material.AIR);
            }
        }, 100L);
    }

    // ─────────── REYNA ───────────
    public void addSoulOrb(Player player) {
        soulOrbs.merge(player.getUniqueId(), 1, Integer::sum);
        player.sendActionBar("§5✦ Soul Orb absorbed! (" + soulOrbs.get(player.getUniqueId()) + " available)");
    }

    private void reynaDevour(Player p) {
        int orbs = soulOrbs.getOrDefault(p.getUniqueId(), 0);
        if (orbs <= 0) { p.sendActionBar("§cNo Soul Orbs! Get a kill first."); return; }
        soulOrbs.put(p.getUniqueId(), orbs - 1);
        double healAmount = Math.min(p.getMaxHealth() - p.getHealth(), 10);
        p.setHealth(p.getHealth() + healAmount);
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0,1,0), 12, 0.5, 0.5, 0.5, 0.05);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.7f);
        p.sendActionBar("§5✦ Devour!");
    }

    private void reynaDismiss(Player p) {
        int orbs = soulOrbs.getOrDefault(p.getUniqueId(), 0);
        if (orbs <= 0) { p.sendActionBar("§cNo Soul Orbs! Get a kill first."); return; }
        soulOrbs.put(p.getUniqueId(), orbs - 1);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 2));
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0,1,0), 30, 0.5, 1, 0.5, 0.2);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
        p.sendActionBar("§5✦ Dismiss!");
    }

    // ─────────── BREACH ───────────
    private void breachFlashpoint(Player p) {
        Location target = p.getLocation().add(p.getLocation().getDirection().multiply(8));
        p.getWorld().spawnParticle(Particle.FLASH, target, 3, 0.5, 0.5, 0.5, 0.1);
        p.getWorld().playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        for (Entity e : p.getNearbyEntities(8, 4, 8)) {
            if (e instanceof Player enemy && !enemy.equals(p)) {
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1));
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                enemy.sendActionBar("§c⚡ Breach Flashpoint!");
            }
        }
        p.sendActionBar("§c✦ Flashpoint!");
    }

    private void breachFaultLine(Player p) {
        Vector dir = p.getLocation().getDirection().setY(0).normalize();
        Location start = p.getLocation();
        for (int i = 1; i <= 10; i++) {
            Location pos = start.clone().add(dir.clone().multiply(i));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                pos.getWorld().spawnParticle(Particle.BLOCK_CRACK, pos, 20, 0.5, 0.5, 0.5, 0,
                        Material.DIRT.createBlockData());
                pos.getWorld().playSound(pos, Sound.BLOCK_STONE_BREAK, 0.4f, 0.5f);
                for (Entity e : pos.getWorld().getNearbyEntities(pos, 2, 2, 2)) {
                    if (e instanceof Player enemy && !enemy.equals(p)) {
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 3));
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 1));
                        enemy.sendActionBar("§c⚡ Breach Fault Line!");
                    }
                }
            }, i * 3L);
        }
        p.sendActionBar("§c✦ Fault Line!");
    }

    // ─────────── Passive application ───────────
    private void applyPassive(Player p, Agent agent) {
        // Remove old passives
        p.removePotionEffect(PotionEffectType.SPEED);
        p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);

        switch (agent) {
            case JETT:    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false)); break;
            case SAGE:    p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, false, false)); break;
            case PHOENIX: // heals on kill - handled in CombatListener
            case REYNA:   // soul orbs on kill - handled in CombatListener
            case BREACH:  // no passive buff
            default:      break;
        }
    }

    // ─────────── Cooldown helpers ───────────
    private boolean checkCooldown(Player p, Agent agent, boolean isQ) {
        UUID id = p.getUniqueId();
        Map<UUID, Long> map = isQ ? qCooldowns : eCooldowns;
        long now = System.currentTimeMillis();
        long last = map.getOrDefault(id, 0L);
        int cdSec = isQ ? agent.getQCooldown() : agent.getECooldown();
        long remaining = (cdSec * 1000L) - (now - last);
        if (remaining > 0) {
            p.sendActionBar("§c⏳ Cooldown: " + (remaining / 1000) + "s");
            return false;
        }
        return true;
    }

    private void setCooldown(Player p, Agent agent, boolean isQ) {
        Map<UUID, Long> map = isQ ? qCooldowns : eCooldowns;
        map.put(p.getUniqueId(), System.currentTimeMillis());
    }

    public Agent getAgent(Player p) { return selectedAgents.get(p.getUniqueId()); }

    public void clearPlayer(Player p) {
        UUID id = p.getUniqueId();
        selectedAgents.remove(id);
        qCooldowns.remove(id);
        eCooldowns.remove(id);
        soulOrbs.remove(id);
        p.removePotionEffect(PotionEffectType.SPEED);
        p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
    }

    public void clearAll() {
        for (UUID id : selectedAgents.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) clearPlayer(p);
        }
        selectedAgents.clear(); qCooldowns.clear(); eCooldowns.clear(); soulOrbs.clear();
    }
}
