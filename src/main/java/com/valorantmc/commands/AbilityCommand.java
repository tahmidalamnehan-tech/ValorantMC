package com.valorantmc.commands;

import com.valorantmc.ValorantPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class AbilityCommand implements CommandExecutor {
    private final ValorantPlugin plugin;
    public AbilityCommand(ValorantPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /vability <q|e>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "q" -> plugin.getGameManager().agentManager.useAbilityQ(p);
            case "e" -> plugin.getGameManager().agentManager.useAbilityE(p);
            default  -> p.sendMessage(ChatColor.RED + "Use /vability q or /vability e");
        }
        return true;
    }
}
