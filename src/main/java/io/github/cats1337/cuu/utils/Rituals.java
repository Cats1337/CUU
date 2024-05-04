package io.github.cats1337.cuu.utils;

import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.events.DragonSword;
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
import org.bukkit.util.EulerAngle;

public class Rituals {
    private static final Logger logger = LogManager.getLogger(DragonSword.class);

    // startRitual - start a ritual
    // itemName - the name of the item
    // cooldownTime - the cooldown time in milliseconds
    // playerName - the name of the player who started the ritual
    // getLocation - get the location of the player

    // start of ritual, itemName, cooldownTime, playerName
    // cooldown time, 30 minutes
    // ticks 20 ticks per second 1200 ticks per minute 30 minutes 36000 ticks

    public static void startRitual(Player p, String itemName, ItemStack itemUb) {
        Location locO = p.getLocation();
        int x1 = locO.getBlockX();
        int y1 = locO.getBlockY();
        int z1 = locO.getBlockZ();

        // Adjust the location to center the display
        double x = x1 + 0.50;
        double y = y1 + 0.00;
        double z = z1 + 0.50;

        Location loc = new Location(locO.getWorld(), x, y, z);


        String world = loc.getWorld().getName();

        // world_the_end -> The End
        if (world.equals("world_the_end")) {
            world = "§d§nThe End§r";
        }
        // world -> Overworld
        if (world.equals("world")) {
            world = "§d§nOverworld§r";
        }
        // world_nether -> Nether
        if (world.equals("world_nether")) {
            world = "§d§nNether§r";
        }

        String ritualTitle = "§6§l" + itemName + " §8@ §5x§8:§r " + x + " §5y§8:§r " + y + " §5z§8:§r " + z + "§8 in " + world;

        Title titler = Title.title(
                Component.text("§5§l§nRitual§r §a§l§nStarted"), // Main title
                Component.text(ritualTitle) // Subtitle
        );
        p.showTitle(titler);

      showBossBar(ritualTitle, ItemManager.getRitualTime("ritual"));
      createHologram(loc, itemName, itemUb, ItemManager.getRitualTime("ritual"));
//        showBossBar(ritualTitle, 17);
//        createHologram(loc, itemName, itemUb, 17);
        ItemManager.setRitualActive(true);

    }

    public static void createHologram(Location loc, String itemName, ItemStack itemUb, int seconds) {
        World world = loc.getWorld();

        ArmorStand hologram = (ArmorStand) world.spawnEntity(loc.clone().add(0, 0.5, 0), EntityType.ARMOR_STAND);
        ArmorStand hologram2 = (ArmorStand) world.spawnEntity(loc.clone().add(0, 0.25, 0), EntityType.ARMOR_STAND);
        ArmorStand itemStand = (ArmorStand) world.spawnEntity(loc.clone().add(0, 0, 0), EntityType.ARMOR_STAND);

        // set block to crafting table
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
        itemStand.setCanPickupItems(false);
        itemStand.setCustomNameVisible(false);
        itemStand.setItem(EquipmentSlot.HAND, itemUb);
        itemStand.setRightArmPose(new EulerAngle(0, -1.5707963, -1.7765134));

        // Set item name on first hologram
        Component itemNameComponent = Component.text("§6§l" + itemName);
        hologram.customName(itemNameComponent);

        // Create final array to hold remaining seconds
        final int[] remainingSeconds = {seconds};

        // Rotation task
        int rotItem = Bukkit.getScheduler().runTaskTimer(CUU.getInstance(), () -> {

            if (remainingSeconds[0] <= 10) {
                // send item into the air, up 7 blocks
                if (itemStand.getLocation().getY() < loc.getY() + 7) {
                    itemStand.teleport(itemStand.getLocation().add(0, 0.20, 0));
                }
                // send item back to the ground, down 7 blocks, with 3 seconds left
                if (remainingSeconds[0] <= 2) {
                    while (itemStand.getLocation().getY() > loc.getY()) {
                        itemStand.teleport(itemStand.getLocation().subtract(0, 0.20, 0));
                    }
                    world.createExplosion(itemStand.getLocation(), 0.0F, false, false);
                }
                // while exactly 6 seconds left, strike lightning
                if (remainingSeconds[0] == 1) {
                    world.strikeLightningEffect(itemStand.getLocation());
                    // since it's 20 ticks per second, only strike every 4 ticks
                }
            }

            // Rotate item
            itemStand.setRotation(itemStand.getLocation().getYaw() + 1, itemStand.getLocation().getPitch());

        }, 0L, 1L).getTaskId();


        // Remaining time task
        int remainTime = Bukkit.getScheduler().runTaskTimer(CUU.getInstance(), () -> {
            int min = remainingSeconds[0] / 60;
            int sec = remainingSeconds[0] % 60;

            // Format remaining time
            String formattedTime = (sec < 10) ? min + ":0" + sec : min + ":" + sec;

            // Set time remaining on second hologram
            Component timeComponent = Component.text("§5Time Remaining: §r" + formattedTime);
            hologram2.customName(timeComponent);

            // Decrement remaining seconds
            remainingSeconds[0]--;
        }, 0L, 20L).getTaskId(); // Run every tick (20 ticks per second)

        // Cleanup task
        Bukkit.getScheduler().runTaskLater(CUU.getInstance(), () -> {
            hologram.remove();
            hologram2.remove();
            itemStand.remove();

            // set block back to air
            loc.getBlock().setType(Material.AIR);

            Bukkit.getScheduler().cancelTask(remainTime);
            Bukkit.getScheduler().cancelTask(rotItem);

            world.dropItemNaturally(loc, itemUb);
            ItemManager.setExists(NameCheck.convertToConfigName(itemName), true);
            ItemManager.setRitualActive(false);

        }, seconds * 20L); // Convert seconds to ticks (20 ticks per second)

    }

    static BossBar bossBar = Bukkit.createBossBar("title", BarColor.BLUE, BarStyle.SEGMENTED_20);
    public static void showBossBar(String title, int seconds) {
        bossBar.setTitle(title);

        // add all players to the bossbar
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
            p.playSound(p.getLocation(), "block.end_portal.spawn", 2F, .05F);
        }

        // reduce bossbar every second, async thread so server doesn't crash, oopsies
        Bukkit.getScheduler().runTaskAsynchronously(CUU.getInstance(), () -> {
            for (int i = seconds; i >= 0; i--) {
                int elapsedSeconds = seconds - i;
                int remainingSeconds = seconds - elapsedSeconds;
                int min = remainingSeconds / 60;
                int sec = remainingSeconds % 60;

                // if sec = 9, add a 0 in front of it
                if (sec < 10) {
                    bossBar.setTitle(title + " §8| §bTime:§r " + min + ":0" + sec);
                } else {
                    bossBar.setTitle(title + " §8| §bTime:§r " + min + ":" + sec);
                }

                double progress = (double) remainingSeconds / seconds;
                bossBar.setProgress(progress);


                // 100% -> 66%  30m -> 20m | Blue
                if (remainingSeconds >= 1200) {
                    bossBar.setColor(BarColor.BLUE);
                    bossBar.setStyle(BarStyle.SEGMENTED_12);
                    // 66% -> 33%  20m -> 10m | Purple
                } else if (remainingSeconds >= 600) {
                    bossBar.setColor(BarColor.PURPLE);
                    bossBar.setStyle(BarStyle.SEGMENTED_10);
                    // 33% -> 66%  10m -> 5m | Pink
                } else if (remainingSeconds >= 300) {
                    bossBar.setColor(BarColor.PINK);
                    bossBar.setStyle(BarStyle.SEGMENTED_6);
                    // 16% -> 0%  5m -> 0m | Green
                } else {
                    bossBar.setColor(BarColor.GREEN);
                    bossBar.setStyle(BarStyle.SOLID);
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    bossBar.addPlayer(p);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted exception", e);
                }
            }

            Title titler = Title.title(
                    Component.text("§5§l§nRitual§r §4§l§nFinished"), // Main title
                    Component.text(title) // Subtitle
            );

            // remove bossbar after countdown is done
            bossBar.removeAll();
            // play complete sound
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showTitle(titler);
                p.playSound(p.getLocation(), "minecraft:ui.toast.challenge_complete", 0.1f, 1);
            }
        });
    }

    public static void removeBossbars(){
        bossBar.removeAll();
        ItemManager.setRitualActive(false);
    }


}
