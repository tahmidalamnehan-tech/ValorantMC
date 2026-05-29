package com.valorantmc.commands;

import com.valorantmc.ValorantPlugin;
import org.bukkit.command.*;

public class StopCommand implements CommandExecutor {
    private final ValorantPlugin plugin;
    public StopCommand(ValorantPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        plugin.getGameManager().stopGame();
        return true;
    }
}
