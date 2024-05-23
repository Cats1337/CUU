package io.github.cats1337.cuu.utils;

import io.github.cats1337.cuu.CUU;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

public class Rituals {
    private static final Logger logger = LogManager.getLogger(Rituals.class);
    private static List<BukkitTask> tasks = new ArrayList<>();
    static BossBar bossBar = Bukkit.createBossBar("title", BarColor.BLUE, BarStyle.SEGMENTED_20);

    public static void startRitual(Player p, String itemName, ItemStack itemUb) {
        Location locO = p.getLocation();
        int x1 = locO.getBlockX();
        int y1 = locO.getBlockY();
        int z1 = locO.getBlockZ();

        double x = x1 + 0.50;
        double y = y1 + 0.00;
        double z = z1 + 0.50;

        Location loc = new Location(locO.getWorld(), x, y, z);

        String world = loc.getWorld().getName();

        if (world.equals("world_the_end")) {
            world = "§d§nThe End§r";
        }
        if (world.equals("world")) {
            world = "§d§nOverworld§r";
        }
        if (world.equals("world_nether")) {
            world = "§d§nNether§r";
        }

        String ritualTitle = "§6§l" + itemName + " §8@ §5x§8:§r " + x + " §5y§8:§r " + y + " §5z§8:§r " + z + "§8 in " + world;

        Title titler = Title.title(
                Component.text("§5§l§nRitual§r §a§l§nStarted"),
                Component.text(ritualTitle)
        );
        p.showTitle(titler);

        showBossBar(ritualTitle, ItemManager.getRitualTime("ritual"));
        createHologram(loc, itemName, itemUb, ItemManager.getRitualTime("ritual"));
        ItemManager.setRitualActive(true);
    }

    public static void createHologram(Location loc, String itemName, ItemStack itemUb, int seconds) {
        World world = loc.getWorld();

        ArmorStand hologram = (ArmorStand) world.spawnEntity(loc.clone().add(0, 0.5, 0), EntityType.ARMOR_STAND);
        ArmorStand hologram2 = (ArmorStand) world.spawnEntity(loc.clone().add(0, 0.25, 0), EntityType.ARMOR_STAND);
        ArmorStand itemStand = (ArmorStand) world.spawnEntity(loc.clone().add(0, 0, 0), EntityType.ARMOR_STAND);

        loc.getBlock().setType(Material.CRAFTING_TABLE);

        hologram.setInvisible(true);
        hologram.setCustomNameVisible(true);
        hologram.setInvulnerable(true);
        hologram.setGravity(false);

        hologram2.setInvisible(true);
        hologram2.setCustomNameVisible(true);
        hologram2.setInvulnerable(true);
        hologram2.setGravity(false);

        itemStand.setInvisible(true);
        itemStand.setCustomNameVisible(true);
        itemStand.setInvulnerable(true);
        itemStand.setArms(true);
        itemStand.setGravity(false);
        itemStand.setVisible(false);
        itemStand.setItem(EquipmentSlot.HAND, itemUb);
        itemStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        itemStand.setCustomNameVisible(false);
        itemStand.setRightArmPose(new EulerAngle(0, -1.5707963, -1.7765134));

        Component itemNameComponent = Component.text("§6§l" + itemName);
        hologram.customName(itemNameComponent);

        final int[] remainingSeconds = {seconds};

        BukkitTask rotItemTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingSeconds[0] <= 10) {
                    if (itemStand.getLocation().getY() < loc.getY() + 7) {
                        itemStand.teleport(itemStand.getLocation().add(0, 0.20, 0));
                    }
                    if (remainingSeconds[0] <= 2) {
                        while (itemStand.getLocation().getY() > loc.getY()) {
                            itemStand.teleport(itemStand.getLocation().subtract(0, 0.20, 0));
                        }
                        world.createExplosion(itemStand.getLocation(), 0.0F, false, false);
                    }
                    if (remainingSeconds[0] == 1) {
                        world.strikeLightningEffect(itemStand.getLocation());
                    }
                }
                itemStand.setRotation(itemStand.getLocation().getYaw() + 1, itemStand.getLocation().getPitch());

                if (!ItemManager.getRitualActive()) {
                    cancel();
                    itemStand.remove();
                    hologram.remove();
                }
            }
        }.runTaskTimer(CUU.getInstance(), 0L, 1L);
        tasks.add(rotItemTask);

        BukkitTask remainTimeTask = new BukkitRunnable() {
            @Override
            public void run() {
                int min = remainingSeconds[0] / 60;
                int sec = remainingSeconds[0] % 60;

                String formattedTime = (sec < 10) ? min + ":0" + sec : min + ":" + sec;

                Component timeComponent = Component.text("§5Time Remaining: §r" + formattedTime);
                hologram2.customName(timeComponent);

                remainingSeconds[0]--;

                if (!ItemManager.getRitualActive()) {
                    cancel();
                }
            }
        }.runTaskTimer(CUU.getInstance(), 0L, 20L);
        tasks.add(remainTimeTask);

        BukkitTask hologramRemovalTask = new BukkitRunnable() {
            @Override
            public void run() {
                hologram.remove();
                hologram2.remove();
                itemStand.remove();

                loc.getBlock().setType(Material.AIR);

                world.dropItemNaturally(loc, itemUb);
                ItemManager.setExists(NameCheck.convertToConfigName(itemName), true);
                ItemManager.setRitualActive(false);
            }
        }.runTaskLater(CUU.getInstance(), seconds * 20L);
        tasks.add(hologramRemovalTask);
    }

    public static void showBossBar(String title, int seconds) {
        bossBar.setTitle(title);

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
            p.playSound(p.getLocation(), "block.end_portal.spawn", 2F, .05F);
        }

        BukkitTask bossBarTask = new BukkitRunnable() {
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

                    // 30 minutes | 1800
                    // 20 minutes | 1200 | aka 33%
                    // 10 minutes | 600 | aka 66%
                    // 5 minutes | 300 | aka 83%

                    // if time remaining is 67% or more, color is blue
                    // if time remaining is 33% or more, color is purple
                    // if time remaining is 17% or more, color is pink
                    // if time remaining is less than 17%, color is green

                    // get 67% of the total time: seconds * 0.67
                    // 1800 * 0.67 = 1206 ~20 minutes
                    // 1800 * 0.33 = 594 ~10 minutes
                    // 1800 * 0.17 = 306 ~5 minutes

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
        }.runTaskAsynchronously(CUU.getInstance());
        tasks.add(bossBarTask);
    }

    public static void cancelRitual() {
        bossBar.removeAll();
        ItemManager.setRitualActive(false);

        for (BukkitTask task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }

        tasks.clear();
    }
}
