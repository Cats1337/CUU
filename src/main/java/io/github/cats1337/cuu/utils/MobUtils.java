package io.github.cats1337.cuu.utils;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.cuu.CUU;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.loot.Lootable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class MobUtils {

    private static final String TEAM_NAME = "doomMob";
    private static Scoreboard scoreboard;
    private static final List<BukkitTask> tasks = new ArrayList<>();
    private static final List<Entity> mobs = new ArrayList<>();

    // Initialize scoreboard in a method to avoid static context issues
    public static void initialize() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            scoreboard = manager.getMainScoreboard();
        } else {
            throw new IllegalStateException("Failed to get the ScoreboardManager");
        }
    }

    // Create doomMob team if it doesn't exist
    public static void createTeam() {
        if (scoreboard.getTeam(TEAM_NAME) == null) {
            Team team = scoreboard.registerNewTeam(TEAM_NAME);
            team.setDisplayName("§4Doom Mob");
            team.setAllowFriendlyFire(false);
        }
    }

    // Summon mobs to the world
    public static void summonMob(Player p, EntityType mob, int amount, int health, int time) {
//        MobUtils.summonMobs(player, EntityType.WITHER_SKELETON, 2, 100, 60);

        // summon amount of mobs around the player, randomize the location (within 3 blocks of the player)
        for (int i = 0; i < amount; i++) {
            Location spawnLocation = p.getLocation().add(Math.random() * 6 - 3, 0, Math.random() * 6 - 3);
            Entity entity = p.getWorld().spawnEntity(spawnLocation, mob);

            if (entity instanceof Lootable lootable) {
                lootable.setLootTable(null);
            }

            if (entity instanceof Damageable doomMob) {
                doomMob.setMaxHealth(health);
                doomMob.setHealth(health);
                doomMob.setCustomName("§b" + p.getName() + "§b's§4§l Summon");
                doomMob.addScoreboardTag("DOOM_MOB");
                doomMob.setCustomNameVisible(false);
                p.playSound(p.getLocation(), "block.respawn_anchor.set_spawn", 2F, 2f);
                addMobToTeam(doomMob);
                mobTimer(p, doomMob, time);
                mobs.add(entity);
            }

        }
        addPlayerToTeam(p);
        showMobTimer(p, "§4§lDoom Mob Timer", 60); // 5 minute cooldown
        // add sound effect, magic sound
        // actionbar countdown for the mobs,

    }

    // Method to add a player to the doomMob team
    public static void addPlayerToTeam(Player p) {
        createTeam(); // Ensure the team exists
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team != null) {
            team.addEntry(p.getName());
        }
    }

    // Method to remove a player from the doomMob team
    public static void removePlayerFromTeam(Player p) {
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team != null && team.hasEntry(p.getName())) {
            team.removeEntry(p.getName());
        }
    }

    // Method to add a mob to the doomMob team
    public static void addMobToTeam(Entity mob) {
        createTeam(); // Ensure the team exists
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team != null) {
            team.addEntry(mob.getUniqueId().toString());
        }
    }

    // Method to remove a mob from the doomMob team
    public static void removeMobFromTeam(Entity mob) {
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team != null && team.hasEntry(mob.getUniqueId().toString())) {
            team.removeEntry(mob.getUniqueId().toString());
        }
    }

    // Delete mob and remove from team
    public static void deleteMob(Entity mob) {
        removeMobFromTeam(mob); // Remove from team
        mob.getLocation().getWorld().playSound(mob.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
        mob.remove(); // Delete mob
        mobs.remove(mob); // Remove from list
    }

    // Mob timer to despawn mobs after a certain amount of time
    public static void mobTimer(Player p, Entity mob, int seconds) {
        BukkitTask mobTask = new BukkitRunnable() {
            @Override
            public void run() {
                deleteMob(mob);
                removePlayerFromTeam(p);
                Text.of("§c§oDoom Mob has despawned!").send(p);
            }
        }.runTaskLater(CUU.getInstance(), seconds * 20L); // Convert seconds to ticks
        tasks.add(mobTask);
    }

    public static void showMobTimer(Player p, String title, int seconds) {
        BossBar bossBar = Bukkit.createBossBar("mob_cooldown", BarColor.BLUE, BarStyle.SEGMENTED_20);
        bossBar.setTitle(title);

        // Ensure that `seconds` is not zero to avoid division by zero.
        if (seconds <= 0) {
            throw new IllegalArgumentException("The duration for the boss bar must be greater than zero.");
        }

        BukkitTask bossBarTask = new BukkitRunnable() {
            int remainingSeconds = seconds;

            @Override
            public void run() {
                if (remainingSeconds < 0) {
                    // Remove the boss bar when the countdown is complete
                    bossBar.removeAll();
                    p.playSound(p.getLocation(), "block.note_block.chime", 1, 1);
                    cancel();
                    return;
                }

                int min = remainingSeconds / 60;
                int sec = remainingSeconds % 60;

                String timeString = (sec < 10) ? min + ":0" + sec : min + ":" + sec;
                bossBar.setTitle(title + " §8| §bTime:§r " + timeString);

                // Update progress
                bossBar.setProgress((double) remainingSeconds / seconds);

                // Update boss bar color and style based on remaining time
                if (remainingSeconds >= seconds * 0.67) {
                    bossBar.setColor(BarColor.BLUE);
                    bossBar.setStyle(BarStyle.SEGMENTED_12);
                } else if (remainingSeconds >= seconds * 0.33) {
                    bossBar.setColor(BarColor.PURPLE);
                    bossBar.setStyle(BarStyle.SEGMENTED_10);
                } else if (remainingSeconds >= seconds * 0.17) {
                    bossBar.setColor(BarColor.PINK);
                    bossBar.setStyle(BarStyle.SEGMENTED_6);
                } else {
                    bossBar.setColor(BarColor.GREEN);
                    bossBar.setStyle(BarStyle.SOLID);
                }

                bossBar.addPlayer(p);

                remainingSeconds--;
            }
        }.runTaskTimer(CUU.getInstance(), 0L, 20L); // Run every second
        tasks.add(bossBarTask);
    }


    // Cancel tasks and cleanup
    public static void cancelTasks() {
        for (BukkitTask task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }
        for (Entity mob : mobs) {
            deleteMob(mob);
        }
        tasks.clear();
    }

}
