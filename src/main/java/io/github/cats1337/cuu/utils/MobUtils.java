package io.github.cats1337.cuu.utils;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.cuu.CUU;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.loot.Lootable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class MobUtils {

    private static final String TEAM_NAME = "doomMob";
    private static Scoreboard scoreboard;

    // Static initializer to set up the scoreboard, ensures scoreboard is set up before any other methods are called, preventing NPEs
    static {
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
            Entity entity = p.getWorld().spawnEntity(p.getLocation().add(Math.random() * 3, 0, Math.random() * 3), mob);

            Lootable lootable = (Lootable) entity;
            lootable.setLootTable(null);

            Damageable doomMob = (Damageable) lootable;
            doomMob.setMaxHealth(health);
            doomMob.setHealth(health);
            doomMob.setCustomName("§b" + p.name() + "§b's§4§l Summon");
            doomMob.addScoreboardTag("DOOM_MOB");
            doomMob.setCustomNameVisible(false);


            // remove weapon

            // remove DeathLootTable

            addMobToTeam(doomMob);
            mobTimer(p, entity, time);

        }
        addPlayerToTeam(p);
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

    public static void deleteMob(Entity mob) {
        removeMobFromTeam(mob); // Remove from team
        mob.getLocation().getWorld().playSound(mob.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
        mob.remove(); // Delete mob
    }

    // Mob timer to despawn mobs after a certain amount of time
    public static void mobTimer(Player p, Entity mob, int seconds) {
        Bukkit.getScheduler().runTaskLater(CUU.getInstance(), () -> {
            deleteMob(mob);
            removePlayerFromTeam(p);
            Text.of("§c§oDoom Mob have despawned!").send(p);
        }, seconds * 20L); // Convert seconds to ticks
    }


}
