package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.utils.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import thirtyvirus.uber.UberItems;

public class DragonSword implements Listener {

    private static final Logger logger = LogManager.getLogger(DragonSword.class);

    @EventHandler
    public void onDragonDeath(EntityDeathEvent e) {

        // make sure the dragon is actually dead and not flying to podium
        // check the phase of the dragon
        // if the dragon is flying to the podium, wait
        // if the dragon is dead, continue

        if (e.getEntityType() == EntityType.ENDER_DRAGON) {
            EnderDragon dragon = (EnderDragon) e.getEntity();

            World world = dragon.getWorld();
            DragonBattle dragonBattle = world.getEnderDragonBattle();

            if (dragonBattle != null) {
                if (!ItemManager.getExists("DOOM_SWORD")) {
                    dragonBattle.generateEndPortal(false);
//                    Location loc = dragonBattle.getEndPortalLocation();
                    Location btmPod = findBottomPodium(world);
                    spawnPodium(btmPod);
                    assert btmPod != null;
                    replaceBlocks(btmPod, Material.RED_STAINED_GLASS);
                    ItemManager.setRitualActive(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH) // Highest priority, meaning it will be called first
    public void onPortalCreateEvent(PortalCreateEvent e) {
        if(e.getEntity() instanceof EnderDragon && e.getEntity().getWorld().getName().equals("world_the_end")){
            e.setCancelled(true);
        }
    }

    // on dragon spawn for the first time
    @EventHandler
    public void onDragonSpawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.ENDER_DRAGON) {
            EnderDragon dragon = (EnderDragon) e.getEntity();
            World world = dragon.getWorld();
            DragonBattle dragonBattle = world.getEnderDragonBattle();

            // check if the dragon has been killed before
            if (dragonBattle != null) {
                if (!ItemManager.getExists("DOOM_SWORD")) {
                    Title titler = Title.title(
                            Component.text("§5§l§nRitual§r §a§l§nStarted"), // Main title
                            Component.text("§6§lDoom Sword" + "§8 in §d§nThe End") // Subtitle
                    );
                    // show title to players
                    Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(titler));
                    dragonBattle.generateEndPortal(false);
                }
            }
        }
    }

    public static void spawnPodium(Location loc) {
        if (loc == null) { return; }
        replaceBlocks(loc, Material.RED_STAINED_GLASS);
        // Wait 180 ticks before attempting to replace the end portal blocks
        Bukkit.getScheduler().runTaskLater(CUU.getInstance(), () -> {
            // Start trying to replace end portal blocks with red stained glass
            int taskid = Bukkit.getScheduler().runTaskTimer(CUU.getInstance(), () ->
                    replaceBlocks(loc, Material.RED_STAINED_GLASS), 0L, 1L).getTaskId();

            // Cancel the task after 25 ticks or until successful
            Bukkit.getScheduler().runTaskLater(CUU.getInstance(), () -> {
                Bukkit.getScheduler().cancelTask(taskid);
                // create dragon sword item
                Location loc2 = loc.clone().add(0, 6, 0);

                if(!ItemManager.getExists("DOOM_SWORD")) {

                    ItemStack itemU = UberItems.getItem("doom_sword").makeItem(1);
                    loc.getWorld().dropItemNaturally(loc2, itemU);

                    Title titler = Title.title(
                            Component.text("§5§l§nRitual§r §4§l§nFinished"), // Main title
                            Component.text("§6§lDoom Sword" + "§8 in §d§nThe End") // Subtitle
                    );

                    showBossBar("§6§lDoom Sword", 900); // 15 minutes = 1800 seconds
                    ItemManager.setExists("DOOM_SWORD", true);
                    ItemManager.setRitualActive(true);

                    // show title to players
                    Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(titler));
                }
            }, 35L);
        }, 180L);

        // wait 15 minutes before replacing the blocks back to end portal
        int taskid = new BukkitRunnable() {
            @Override
            public void run() {
                replaceBlocks(loc, Material.END_PORTAL);
            }
        }.runTaskLater(CUU.getInstance(), ItemManager.getRitualTime("dragon") * 20L).getTaskId();

        // If the ritual is no longer active, cancel the task, and replace the blocks back to end portal
        if (!ItemManager.getRitualActive()) {
            Bukkit.getScheduler().cancelTask(taskid);
            replaceBlocks(loc, Material.END_PORTAL);
        }

    }

    // Looking at podium from top down
    // X = bedrock, 0 = air/portal
    // X 0 0 0 X
    // 0 0 0 0 0
    // 0 0 X 0 0
    // 0 0 0 0 0
    // X 0 0 0 X

    // replace blocks in a 2x2 area. replaceBlocks(Location loc, Material toReplace, Material replaceWith)
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

    // iterate through all y values and find the lowest bedrock block of the end portal
    // portal is x: 0 y: ?? z: 0
    // start from bottom and go up
    // find bottomPodium ~ 64?
    public static Location findBottomPodium(World world) {
        // get very bottom bedrock block at x: 0, z: 0

        for (int i = 0; i < 256; i++) {
            Block block = world.getBlockAt(0, i, 0);
            if (block.getType() == Material.BEDROCK) {
                Location loc = new Location(world, 0, i, 0);
                loc.add(0, 1, 0); // add 1 to y so it's the portal level
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

        int taskid = new BukkitRunnable() {
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

                    if (remainingSeconds >= 1200) {
                        bossBar.setColor(BarColor.BLUE);
                        bossBar.setStyle(BarStyle.SEGMENTED_12);
                    } else if (remainingSeconds >= 600) {
                        bossBar.setColor(BarColor.PURPLE);
                        bossBar.setStyle(BarStyle.SEGMENTED_10);
                    } else if (remainingSeconds >= 300) {
                        bossBar.setColor(BarColor.PINK);
                        bossBar.setStyle(BarStyle.SEGMENTED_6);
                    } else {
                        bossBar.setColor(BarColor.GREEN);
                        bossBar.setStyle(BarStyle.SOLID);
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        bossBar.addPlayer(p);
                    }

                    // check if the ritual is still active
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
        }.runTaskAsynchronously(CUU.getInstance()).getTaskId();

    }

    public static void cancelDsword(){
        bossBar.removeAll();
        ItemManager.setRitualActive(false);
        replaceBlocks(findBottomPodium(Bukkit.getWorld("world_the_end")), Material.END_PORTAL);
    }

}
