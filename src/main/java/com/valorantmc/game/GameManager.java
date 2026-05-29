package com.valorantmc.game;

import com.valorantmc.ValorantPlugin;
import com.valorantmc.agents.AgentManager;
import com.valorantmc.guns.GunManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    public enum GameState { LOBBY, BUY_PHASE, ROUND_ACTIVE, ROUND_END, GAME_OVER }
    public enum Team { ATTACK, DEFENSE }

    private final ValorantPlugin plugin;
    public final AgentManager agentManager;
    public final GunManager   gunManager;

    private GameState state = GameState.LOBBY;

    // Team rosters
    private final Set<UUID> attackers  = new HashSet<>();
    private final Set<UUID> defenders  = new HashSet<>();

    // Round tracking
    private int currentRound   = 0;
    private int attackerWins   = 0;
    private int defenderWins   = 0;
    private static final int ROUNDS_TO_WIN = 13;
    private static final int BUY_PHASE_SECONDS = 30;
    private static final int ROUND_SECONDS      = 100;

    // Economy
    private final Map<UUID, Integer> credits = new HashMap<>();
    private static final int STARTING_CREDITS = 800;
    private static final int WIN_CREDITS       = 3000;
    private static final int LOSS_CREDITS      = 1900;
    private static final int KILL_CREDITS      = 200;

    // Alive players per round
    private final Set<UUID> alive = new HashSet<>();

    private BukkitTask timerTask;
    private int timerSeconds;

    public GameManager(ValorantPlugin plugin) {
        this.plugin = plugin;
        this.agentManager = new AgentManager(plugin);
        this.gunManager   = new GunManager(plugin);
    }

    // ─── Game lifecycle ───────────────────────────────────────
    public boolean startGame() {
        if (state != GameState.LOBBY) return false;
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.size() < 2) return false;

        // Auto-assign teams
        attackers.clear(); defenders.clear(); credits.clear();
        Collections.shuffle(online);
        for (int i = 0; i < online.size(); i++) {
            UUID id = online.get(i).getUniqueId();
            if (i % 2 == 0) attackers.add(id);
            else             defenders.add(id);
            credits.put(id, STARTING_CREDITS);
        }

        currentRound = 0; attackerWins = 0; defenderWins = 0;
        broadcast("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcast("§6§l  VALORANT MC §e- Game Starting!");
        broadcast("§7  First to §e" + ROUNDS_TO_WIN + " §7round wins wins!");
        broadcast("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        startNextRound();
        return true;
    }

    public void stopGame() {
        cancelTimer();
        gunManager.clearAll();
        agentManager.clearAll();
        attackers.clear(); defenders.clear(); alive.clear();
        state = GameState.LOBBY;
        broadcast("§cGame stopped.");
    }

    // ─── Round cycle ──────────────────────────────────────────
    private void startNextRound() {
        currentRound++;
        state = GameState.BUY_PHASE;
        alive.clear();

        // Restore all players to full HP + inventory
        for (Player p : getOnlinePlayers()) {
            p.setHealth(20);
            p.setFoodLevel(20);
            p.getInventory().clear();
            alive.add(p.getUniqueId());
        }

        // Give Classic to everyone for free
        for (Player p : getOnlinePlayers()) {
            gunManager.giveGun(p, com.valorantmc.guns.Gun.CLASSIC);
        }

        broadcast("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcast("§6§l  ROUND " + currentRound);
        broadcast("§a Attackers: " + attackerWins + "  §c  Defenders: " + defenderWins);
        broadcast("§7  BUY PHASE — " + BUY_PHASE_SECONDS + " seconds");
        broadcast("§7  Use §e/vbuy <gun> §7to purchase weapons.");
        broadcast("§7  Credits: " + creditsDisplay());
        broadcast("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        timerSeconds = BUY_PHASE_SECONDS;
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            timerSeconds--;
            if (timerSeconds <= 5 && timerSeconds > 0) {
                broadcast("§e⏱ Round starts in §f" + timerSeconds + "§e...");
            } else if (timerSeconds <= 0) {
                cancelTimer();
                startRound();
            }
        }, 20L, 20L);
    }

    private void startRound() {
        state = GameState.ROUND_ACTIVE;
        broadcast("§c§l  ROUND START — FIGHT!");
        timerSeconds = ROUND_SECONDS;
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            timerSeconds--;
            if (timerSeconds == 30) broadcast("§e⏱ 30 seconds remaining!");
            if (timerSeconds == 10) broadcast("§c⏱ 10 seconds remaining!");
            if (timerSeconds <= 0) {
                cancelTimer();
                // Time up - defenders win
                endRound(Team.DEFENSE, "Time expired");
            }
        }, 20L, 20L);
    }

    public void onPlayerDeath(Player victim) {
        if (state != GameState.ROUND_ACTIVE) return;
        alive.remove(victim.getUniqueId());
        victim.setGameMode(GameMode.SPECTATOR);
        victim.sendMessage("§c✖ You were eliminated. Spectating...");

        // Check win condition
        boolean attackersAlive  = alive.stream().anyMatch(attackers::contains);
        boolean defendersAlive  = alive.stream().anyMatch(defenders::contains);

        if (!attackersAlive)  { cancelTimer(); endRound(Team.DEFENSE, "All attackers eliminated"); }
        else if (!defendersAlive) { cancelTimer(); endRound(Team.ATTACK, "All defenders eliminated"); }
    }

    private void endRound(Team winner, String reason) {
        state = GameState.ROUND_END;
        if (winner == Team.ATTACK) attackerWins++;
        else                       defenderWins++;

        String winColor = winner == Team.ATTACK ? "§a" : "§c";
        broadcast("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcast(winColor + "§l  " + winner.name() + "S WIN! §7(" + reason + ")");
        broadcast("§a Attackers: " + attackerWins + "  §c  Defenders: " + defenderWins);
        broadcast("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Award credits
        for (Player p : getOnlinePlayers()) {
            UUID id = p.getUniqueId();
            boolean isWinner = (winner == Team.ATTACK && attackers.contains(id))
                    || (winner == Team.DEFENSE && defenders.contains(id));
            int earned = isWinner ? WIN_CREDITS : LOSS_CREDITS;
            credits.merge(id, earned, Integer::sum);
            p.sendMessage((isWinner ? "§a" : "§7") + "  +" + earned + "¢  (Total: " + credits.get(id) + "¢)");
        }

        // Check game over
        if (attackerWins >= ROUNDS_TO_WIN || defenderWins >= ROUNDS_TO_WIN) {
            Bukkit.getScheduler().runTaskLater(plugin, this::endGame, 100L);
        } else {
            // Swap sides every 12 rounds
            if (currentRound == 12) {
                Set<UUID> tmp = new HashSet<>(attackers);
                attackers.clear(); attackers.addAll(defenders);
                defenders.clear(); defenders.addAll(tmp);
                broadcast("§6  SIDES SWAPPED!");
            }
            Bukkit.getScheduler().runTaskLater(plugin, this::startNextRound, 100L);
        }
    }

    private void endGame() {
        Team winner = attackerWins > defenderWins ? Team.ATTACK : Team.DEFENSE;
        broadcast("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcast("§6§l  GAME OVER!");
        broadcast("§f  Winner: " + (winner == Team.ATTACK ? "§a" : "§c") + winner.name() + "S");
        broadcast("§f  " + attackerWins + " - " + defenderWins);
        broadcast("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        // Reset players
        for (Player p : getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20);
        }
        stopGame();
    }

    // ─── Economy ──────────────────────────────────────────────
    public boolean buyGun(Player player, com.valorantmc.guns.Gun gun) {
        if (state != GameState.BUY_PHASE) {
            player.sendMessage("§cCan only buy during the buy phase!");
            return false;
        }
        int c = credits.getOrDefault(player.getUniqueId(), 0);
        if (c < gun.getPrice()) {
            player.sendMessage("§cNot enough credits! Need §e" + gun.getPrice() + "¢ §cbut you have §e" + c + "¢");
            return false;
        }
        credits.put(player.getUniqueId(), c - gun.getPrice());
        gunManager.giveGun(player, gun);
        player.sendMessage("§aPurchased §e" + gun.getDisplayName() + " §afor §e" + gun.getPrice() + "¢. Remaining: §e" + credits.get(player.getUniqueId()) + "¢");
        return true;
    }

    public void onKill(Player killer, Player victim) {
        credits.merge(killer.getUniqueId(), KILL_CREDITS, Integer::sum);
        killer.sendMessage("§a+§e" + KILL_CREDITS + "¢ §7kill bonus");

        // Agent-specific kill effects
        com.valorantmc.agents.Agent killerAgent = agentManager.getAgent(killer);
        if (killerAgent != null) {
            switch (killerAgent) {
                case PHOENIX -> killer.setHealth(Math.min(killer.getMaxHealth(), killer.getHealth() + 4));
                case REYNA   -> agentManager.addSoulOrb(killer);
                default -> {}
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────
    public void cancelTimer() {
        if (timerTask != null) { timerTask.cancel(); timerTask = null; }
    }

    private String creditsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Player p : getOnlinePlayers()) {
            sb.append("§e").append(p.getName()).append(": §f").append(credits.getOrDefault(p.getUniqueId(), 0)).append("¢  ");
        }
        return sb.toString();
    }

    private void broadcast(String msg) { Bukkit.broadcastMessage(msg); }

    public List<Player> getOnlinePlayers() {
        List<Player> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) list.add(p);
        return list;
    }

    public boolean isAlive(Player p)    { return alive.contains(p.getUniqueId()); }
    public boolean isInGame(Player p)   { return attackers.contains(p.getUniqueId()) || defenders.contains(p.getUniqueId()); }
    public GameState getState()         { return state; }
    public int getCurrentRound()        { return currentRound; }
    public Team getTeam(Player p)       { return attackers.contains(p.getUniqueId()) ? Team.ATTACK : Team.DEFENSE; }
}
