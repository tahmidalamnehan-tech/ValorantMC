package com.valorantmc.listeners;

import com.valorantmc.ValorantPlugin;
import com.valorantmc.game.GameManager;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;

public class RoundListener implements Listener {

    private final ValorantPlugin plugin;
    private final GameManager gm;

    public RoundListener(ValorantPlugin plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    // Prevent natural fall damage during buy phase
    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!gm.isInGame(p)) return;
        if (gm.getState() != GameManager.GameState.ROUND_ACTIVE) {
            e.setCancelled(true);
        }
    }

    // Prevent food drain during game
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (gm.isInGame(p)) e.setCancelled(true);
    }

    // Prevent item drops from guns
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (gm.isInGame(e.getPlayer())) e.setCancelled(true);
    }
}
