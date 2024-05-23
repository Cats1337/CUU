package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.utils.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import thirtyvirus.uber.UberItems;

import java.util.ArrayList;
import java.util.List;

public class DragonSword implements Listener {

    private static final Logger logger = LogManager.getLogger(DragonSword.class);
    public static BukkitRunnable dsBossBar;
    private static List<BukkitTask> tasks = new ArrayList<>();

    @EventHandler
    public void onDragonDeath(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.ENDER_DRAGON) {
            EnderDragon dragon = (EnderDragon) e.getEntity();
            World world = dragon.getWorld();
            DragonBattle dragonBattle = world.getEnderDragonBattle();

            if (dragonBattle != null && !ItemManager.getExists("DOOM_SWORD")) {
                dragonBattle.generateEndPortal(false);
                Location btmPod = findBottomPodium(world);
                spawnPodium(btmPod);
                assert btmPod != null;
                replaceBlocks(btmPod, Material.RED_STAINED_GLASS);
                ItemManager.setRitualActive(true);
            }
        }
    }

    @EventHandler
    public void onDragonSpawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.ENDER_DRAGON) {
            EnderDragon dragon = (EnderDragon) e.getEntity();
            World world = dragon.getWorld();
            DragonBattle dragonBattle = world.getEnderDragonBattle();

            if (dragonBattle != null && !ItemManager.getExists("DOOM_SWORD")) {
                Title titler = Title.title(
                        Component.text("§5§l§nRitual§r §a§l§nStarted"),
                        Component.text("§6§lDoom Sword" + "§8 in §d§nThe End")
                );
                Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(titler));
                dragonBattle.generateEndPortal(false);
            }
        }
    }

    public static void spawnPodium(Location loc) {
        if (loc == null) {
            return;
        }
        BukkitTask spWait = new BukkitRunnable() {
            @Override
            public void run() {
                BukkitTask predsr = new BukkitRunnable() {
                    @Override
                    public void run() {
                        replaceBlocks(loc, Material.RED_STAINED_GLASS);
                    }
                }.runTaskTimer(CUU.getInstance(), 0L, 1L);
                tasks.add(predsr);

                BukkitTask dsrHandler = new BukkitRunnable() {
                    @Override
                    public void run() {
                        predsr.cancel();
                        tasks.remove(predsr); // Remove the task from the list, since it's no longer running
                        Location loc2 = loc.clone().add(0, 6, 0);

                        if (!ItemManager.getExists("DOOM_SWORD")) {
                            ItemStack itemU = UberItems.getItem("doom_sword").makeItem(1);
                            loc.getWorld().dropItemNaturally(loc2, itemU);

                            Title titler = Title.title(
                                    Component.text("§5§l§nRitual§r §4§l§nFinished"),
                                    Component.text("§6§lDoom Sword" + "§8 in §d§nThe End")
                            );

                            showBossBar("§6§lDoom Sword", ItemManager.getRitualTime("dragon"));
                            ItemManager.setExists("DOOM_SWORD", true);
                            ItemManager.setRitualActive(true);

                            Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(titler));
                        }
                    }
                }.runTaskLater(CUU.getInstance(), 35L);
                tasks.add(dsrHandler);
            }
        }.runTaskLater(CUU.getInstance(), 180L);
        tasks.add(spWait);

        BukkitTask rbTimer = new BukkitRunnable() {
            @Override
            public void run() {
                replaceBlocks(loc, Material.END_PORTAL);
            }
        }.runTaskLater(CUU.getInstance(), ItemManager.getRitualTime("dragon") * 20L);
        tasks.add(rbTimer);
    }

    public static void replaceBlocks(Location loc, Material replaceWith) {
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                Block block = world.getBlockAt(x + i, y, z + j);
                if (block.getType() != Material.BEDROCK) {
                    block.setType(replaceWith);
                }
            }
        }
    }

    public static Location findBottomPodium(World world) {
        for (int i = 0; i < 256; i++) {
            Block block = world.getBlockAt(0, i, 0);
            if (block.getType() == Material.BEDROCK) {
                Location loc = new Location(world, 0, i, 0);
                loc.add(0, 1, 0);
                return loc;
            }
        }
        return null;
    }

    static BossBar bossBar = Bukkit.createBossBar("title", BarColor.BLUE, BarStyle.SEGMENTED_20);

    public static void showBossBar(String title, int seconds) {
        bossBar.setTitle(title);

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
            p.playSound(p.getLocation(), "block.end_portal.spawn", 2F, .05F);
        }

        dsBossBar = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = seconds; i >= 0; i--) {
                    int elapsedSeconds = seconds - i;
                    int remainingSeconds = seconds - elapsedSeconds;
                    int min = remainingSeconds / 60;
                    int sec = remainingSeconds % 60;

                    String timeString = (sec < 10) ? min + ":0" + sec : min + ":" + sec;
                    bossBar.setTitle(title + " §8| §bTime:§r " + timeString);
                    bossBar.setProgress((double) remainingSeconds / seconds);

                    // 15 minutes | 900 | 100%
                    // 10 minutes | 600 | aka 33%
                    // 5 minutes | 300 | aka 66%
                    // 2 minutes | 120 | aka 83%

                    // if time remaining is 67% or more, color is blue
                    // if time remaining is 33% or more, color is purple
                    // if time remaining is 17% or more, color is pink
                    // if time remaining is less than 17%, color is green

                    // get 67% of the total time: seconds * 0.67
                    // 900 * 0.67 = 603 ~10 minutes
                    // 900 * 0.33 = 297 ~5 minutes
                    // 900 * 0.17 = 153 ~2 minutes

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

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        bossBar.addPlayer(p);
                    }

                    if (!ItemManager.getRitualActive()) {
                        bossBar.removeAll();
                        cancel();
                        return;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exception", e);
                    }
                }

                Title titler = Title.title(
                        Component.text("§5§l§nRitual§r §4§l§nFinished"),
                        Component.text(title)
                );

                bossBar.removeAll();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showTitle(titler);
                    p.playSound(p.getLocation(), "minecraft:ui.toast.challenge_complete", 0.1f, 1);
                }
            }
        };

        dsBossBar.runTaskAsynchronously(CUU.getInstance());
    }

    public static void cancelDsword() {
        bossBar.removeAll();
        ItemManager.setRitualActive(false);
        replaceBlocks(findBottomPodium(Bukkit.getWorld("world_the_end")), Material.END_PORTAL);

        // Cancel BukkitRunnable
        if (dsBossBar != null) {
            dsBossBar.cancel();
        }

        // Cancel BukkitTasks
        for (BukkitTask task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }

        // Clear the list
        tasks.clear();
    }
}
