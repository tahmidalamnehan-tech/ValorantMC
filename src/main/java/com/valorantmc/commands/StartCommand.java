package com.valorantmc.commands;

import com.valorantmc.ValorantPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {
    private final ValorantPlugin plugin;
    public StartCommand(ValorantPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("valorantmc.start") && !(sender instanceof Player)) {
            sender.sendMessage("No permission.");
            return true;
        }
        boolean started = plugin.getGameManager().startGame();
        if (!started) sender.sendMessage(ChatColor.RED + "Could not start! Need at least 2 players, and no active game.");
        return true;
    }
}
