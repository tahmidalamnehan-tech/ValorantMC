package com.valorantmc.listeners;

import com.valorantmc.ValorantPlugin;
import com.valorantmc.game.GameManager;
import com.valorantmc.guns.Gun;
import com.valorantmc.guns.GunManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CombatListener implements Listener {

    private final ValorantPlugin plugin;
    private final GameManager gm;
    private final GunManager  gunManager;

    public CombatListener(ValorantPlugin plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
        this.gunManager = gm.gunManager;
    }

    // Right-click to shoot, sneak+right-click to reload
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!gm.isInGame(player)) return;
        if (gm.getState() != GameManager.GameState.ROUND_ACTIVE) return;

        Gun gun = gunManager.getEquippedGun(player);
        if (gun == null) return;

        org.bukkit.event.block.Action action = e.getAction();
        boolean isRightClick = action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                || action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
        if (!isRightClick) return;

        e.setCancelled(true);

        if (player.isSneaking()) {
            gunManager.startReload(player);
            return;
        }

        if (!gunManager.tryShoot(player)) return;

        // Ray-trace for hit detection
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                50,
                entity -> entity instanceof Player && !entity.equals(player));

        if (result != null && result.getHitEntity() instanceof Player victim) {
            if (!gm.isInGame(victim) || !gm.isAlive(victim)) return;

            // Headshot bonus: if ray hits top third of entity, x1.5 damage
            double hitY = result.getHitPosition().getY() - victim.getLocation().getY();
            boolean headshot = hitY > 1.4;
            int damage = headshot ? (int)(gun.getDamage() * 1.5) : gun.getDamage();
            // Clamp to 1-heart minimum
            damage = Math.max(damage, 1);

            double newHealth = Math.max(0, victim.getHealth() - damage);
            victim.setHealth(newHealth);

            // Feedback
            String hitMsg = headshot
                    ? "§c§l✦ HEADSHOT! §7" + victim.getName() + " §c-" + damage + "hp"
                    : "§e✦ Hit §7" + victim.getName() + " §c-" + damage + "hp";
            player.sendActionBar(hitMsg);
            victim.sendActionBar("§c▶ Hit by " + player.getName() + " §7(" + gun.getDisplayName() + ") -" + damage + "hp");

            // Particles on hit
            victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 12, 0.3, 0.3, 0.3, 0.1);
            if (headshot) {
                victim.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, victim.getLocation().add(0, 1.8, 0), 5, 0.2, 0.2, 0.2, 0.05);
            }

            if (newHealth <= 0) {
                handleKill(player, victim);
            }
        } else {
            // Miss - show tracer particle
            Vector dir = player.getLocation().getDirection();
            Location loc = player.getEyeLocation();
            for (int i = 0; i < 20; i++) {
                loc = loc.add(dir.clone().multiply(2));
                if (loc.getBlock().getType().isSolid()) break;
                player.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
            }
        }
    }

    private void handleKill(Player killer, Player victim) {
        Bukkit.broadcastMessage("§c✖ §f" + victim.getName() + " §7was eliminated by §f" + killer.getName()
                + " §7[" + gunManager.getEquippedGun(killer).getDisplayName() + "]");

        // Drop effect
        victim.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, victim.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.8f);

        gm.onKill(killer, victim);
        gm.onPlayerDeath(victim);
    }

    // Prevent normal Minecraft damage during buy phase
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!gm.isInGame(victim)) return;
        if (gm.getState() == GameManager.GameState.BUY_PHASE) {
            e.setCancelled(true);
        }
        // Cancel projectile damage - all damage comes from tryShoot
        if (e.getDamager() instanceof Projectile || e.getDamager() instanceof Arrow) {
            e.setCancelled(true);
        }
    }

    // Prevent natural deaths from resetting; we handle death ourselves
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!gm.isInGame(e.getEntity())) return;
        e.setDeathMessage(null);
        e.getDrops().clear();
        e.setDroppedExp(0);
    }
}
