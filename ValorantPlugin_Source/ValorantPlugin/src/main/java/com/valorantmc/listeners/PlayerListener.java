package com.valorantmc.listeners;

import com.valorantmc.ValorantPlugin;
import com.valorantmc.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final ValorantPlugin plugin;

    public PlayerListener(ValorantPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        e.getPlayer().sendMessage(ChatColor.YELLOW + "  Welcome to §lVALORANT MC§e!");
        e.getPlayer().sendMessage(ChatColor.GRAY + "  /vagent <name>   - Pick agent");
        e.getPlayer().sendMessage(ChatColor.GRAY + "  /vstart          - Start game");
        e.getPlayer().sendMessage(ChatColor.GRAY + "  /vbuy <gun>      - Buy a gun");
        e.getPlayer().sendMessage(ChatColor.GRAY + "  /vability q/e    - Use ability");
        e.getPlayer().sendMessage(ChatColor.GRAY + "  Agents: Jett, Sage, Phoenix, Reyna, Breach");
        e.getPlayer().sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        GameManager gm = plugin.getGameManager();
        if (gm.isInGame(e.getPlayer())) {
            gm.onPlayerDeath(e.getPlayer());
            gm.gunManager.clearPlayer(e.getPlayer());
            gm.agentManager.clearPlayer(e.getPlayer());
        }
    }
}
