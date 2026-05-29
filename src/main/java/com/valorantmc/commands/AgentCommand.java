package com.valorantmc.commands;

import com.valorantmc.ValorantPlugin;
import com.valorantmc.agents.Agent;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class AgentCommand implements CommandExecutor {
    private final ValorantPlugin plugin;
    public AgentCommand(ValorantPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "Available agents: §fJett, Sage, Phoenix, Reyna, Breach");
            p.sendMessage(ChatColor.GRAY + "Usage: /vagent <name>");
            return true;
        }
        Agent agent = Agent.fromName(args[0]);
        if (agent == null) {
            p.sendMessage(ChatColor.RED + "Unknown agent! Options: Jett, Sage, Phoenix, Reyna, Breach");
            return true;
        }
        plugin.getGameManager().agentManager.selectAgent(p, agent);
        return true;
    }
}
