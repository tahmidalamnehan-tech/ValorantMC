package com.valorantmc.commands;

import com.valorantmc.ValorantPlugin;
import com.valorantmc.guns.Gun;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BuyCommand implements CommandExecutor {
    private final ValorantPlugin plugin;
    public BuyCommand(ValorantPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /vbuy <gun>");
            p.sendMessage(ChatColor.GRAY + "Guns: Classic(free) Shorty(200) Sheriff(800)");
            p.sendMessage(ChatColor.GRAY + "      Spectre(1600) Bucky(850) Phantom(2900) Vandal(2900) Operator(4700)");
            return true;
        }
        Gun gun = Gun.fromName(args[0]);
        if (gun == null) { p.sendMessage(ChatColor.RED + "Unknown gun! Try /vbuy for a list."); return true; }
        plugin.getGameManager().buyGun(p, gun);
        return true;
    }
}
