package com.valorantmc.commands;

import com.valorantmc.ValorantPlugin;
import com.valorantmc.guns.Gun;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class GunCommand implements CommandExecutor {
    private final ValorantPlugin plugin;
    public GunCommand(ValorantPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "Guns: Classic(free), Shorty(200), Sheriff(800),");
            p.sendMessage(ChatColor.YELLOW + "       Spectre(1600), Phantom(2900), Vandal(2900), Operator(4700)");
            p.sendMessage(ChatColor.GRAY + "Usage: /vgun <name>");
            return true;
        }
        Gun gun = Gun.fromName(args[0]);
        if (gun == null) { p.sendMessage(ChatColor.RED + "Unknown gun!"); return true; }
        plugin.getGameManager().gunManager.giveGun(p, gun);
        return true;
    }
}
