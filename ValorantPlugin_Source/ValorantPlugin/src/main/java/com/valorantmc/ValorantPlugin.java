package com.valorantmc;

import com.valorantmc.commands.*;
import com.valorantmc.game.GameManager;
import com.valorantmc.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public class ValorantPlugin extends JavaPlugin {

    private static ValorantPlugin instance;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        gameManager = new GameManager(this);

        // Register commands
        getCommand("vagent").setExecutor(new AgentCommand(this));
        getCommand("vgun").setExecutor(new GunCommand(this));
        getCommand("vstart").setExecutor(new StartCommand(this));
        getCommand("vstop").setExecutor(new StopCommand(this));
        getCommand("vbuy").setExecutor(new BuyCommand(this));
        getCommand("vability").setExecutor(new AbilityCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new RoundListener(this), this);

        getLogger().info("ValorantMC Plugin enabled! 5 agents loaded.");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.stopGame();
        getLogger().info("ValorantMC Plugin disabled.");
    }

    public static ValorantPlugin getInstance() { return instance; }
    public GameManager getGameManager() { return gameManager; }
}
