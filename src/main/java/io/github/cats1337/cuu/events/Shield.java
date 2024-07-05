package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.utils.ItemManager;
import io.github.cats1337.cuu.utils.NameCheck;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Shield implements Listener {
    private static final HashMap<UUID, Long> cooldown = new HashMap<>();
    private static final List<UUID> charged = new ArrayList<>();
    private final List<UUID> blocking = new ArrayList<>();
    private static final long COOLDOWN_TIME = ItemManager.getConfigInt("shieldCooldown") * 1000L; // 30 seconds
    private static final int CHARGE_TIME = ItemManager.getConfigInt("shieldCharge"); // 5 seconds
    private static final List<BukkitTask> tasks = new ArrayList<>();

    public static void clearCharged(){
        charged.clear();
    }

    @EventHandler
    public void onShieldDmg(EntityDamageByEntityEvent e) {
        // This triggers anytime, not just when a shield is broken...
        if (!(e.getEntity() instanceof Player p)) return;

        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        ItemStack offHandItem = p.getInventory().getItemInOffHand();

        if (isDoomShield(mainHandItem) || isDoomShield(offHandItem)) {
            if(p.hasCooldown(Material.SHIELD)) {
                return;
            }
            if(Math.random() < ItemManager.getConfigInt("shieldChance")) { // 10% chance
//                launchShockwave(p);
                if (e.getDamager() instanceof LivingEntity attacker) {
                    attacker.setVelocity(attacker.getLocation().getDirection().multiply(-1.5).setY(0.75));
                }
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action action = e.getAction();
        ItemStack item = e.getItem();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (!isDoomShield(item)) return;

        if (e.getHand() == EquipmentSlot.HAND || e.getHand() == EquipmentSlot.OFF_HAND){
            startChargingShield(p);
            handleBlocking(p);
        }

        // when stop blocking (shield)

    }

    private boolean isDoomShield(ItemStack item) {
        return item != null && item.getType() == Material.SHIELD && NameCheck.extractItemName(item).equals("Doom Shield");
    }

    private void handleBlocking(Player p){
        if (isOnCooldown(p)) {
            return;
        }

        BukkitTask blocker = new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isBlocking() && charged.contains(p.getUniqueId())) {
                    launchShockwave(p);
                    charged.remove(p.getUniqueId());
                    blocking.remove(p.getUniqueId());
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                    this.cancel();
                }
                if (blocking.contains(p.getUniqueId())) {
                    this.cancel();
                    handleBlocking(p);
                }
            }
        }.runTaskTimer(CUU.getInstance(), 1, 1);
        tasks.add(blocker);

    }

    private void startChargingShield(Player p) {
        if (isOnCooldown(p)) {
            p.sendMessage("§cDoom Shield is still on cooldown!");
            return;
        } else if (p.isBlocking() && !charged.contains(p.getUniqueId())){
//            When player starts blocking, if it's the doomshield, add then to the blocking List, keep looping until the player stops blocking, or if they have cooldown. Once player stops blocking launchShockwave, only if they don't have a cooldown.
            blocking.add(p.getUniqueId());
            handleBlocking(p);
        }
        p.sendMessage(String.valueOf(charged.contains(p)));
        showChargingProgress(p);

        BukkitTask shieldCharge = new BukkitRunnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                ticksElapsed++;

                if (ticksElapsed >= CHARGE_TIME * 20) { // if after 5 seconds
//                    launchShockwave(p, shield);
                    if(p.isBlocking() && !cooldown.containsKey(p.getUniqueId())){
                        charged.add(p.getUniqueId());
                        p.playSound(p.getLocation(), "block.note_block.chime", 2F, .5F);
                        p.sendMessage(String.valueOf(charged.contains(p)));
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(CUU.getInstance(), 0, 1);
        tasks.add(shieldCharge);
    }

    private void showChargingProgress(Player p) {
        BukkitTask shieldProg = new BukkitRunnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                ticksElapsed++;

                if (ticksElapsed <= CHARGE_TIME * 20) {
                    int progress = ticksElapsed / (CHARGE_TIME * 20 / 5); // 5 segments
                    if (p.isBlocking()){
                        p.sendActionBar(Component.text("§eCharging Shield: §c" + getProgressBar(progress) + " §b" + (CHARGE_TIME - ticksElapsed / 20) + "s"));
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(CUU.getInstance(), 0, 1);
        tasks.add(shieldProg);
    }

    private String getProgressBar(int progress) {
        return switch (progress) {
            case 1 -> "§c■§7■■■■";
            case 2 -> "§6■■§7■■■";
            case 3 -> "§e■■■§7■■";
            case 4 -> "§a■■■■§7■";
            case 5 -> "§2■■■■■";
            default -> "§7■■■■■";
        };
    }

    private void launchShockwave(Player p) {
        if (isOnCooldown(p) || !charged.contains(p.getUniqueId())) {
            p.sendMessage("Launch Shockwave");
            return;
        }

        charged.remove(p.getUniqueId());
        blocking.remove(p.getUniqueId());

        createShockwave(p, 7, 3);

        p.playSound(p.getLocation(), "entity.wither.break_block", 2F, .1F);

        Location shockwaveOrigin = p.getLocation().add(p.getLocation().getDirection());
        double range = 7.0;

        for (Entity entity : p.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(5 * 20, 2));
                livingEntity.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(5 * 20, 1));

                entity.setVelocity(entity.getLocation().toVector().subtract(shockwaveOrigin.toVector()).normalize().multiply(1.5));
            }
        }

        setCooldown(p);
        p.sendActionBar(Component.empty());
        p.removePotionEffect(PotionEffectType.SLOW);

        new Thread(() -> {
            showCooldown(p, "Doom Shield", ItemManager.getConfigInt("shieldCooldown"));
        }).start();
    }


    private final Set<FallingBlock> trackedFallingBlocks = new HashSet<>();

    private void createShockwave(Player player, double maxRadius, long delayBetweenWaves) {
        Vector origin = player.getLocation().toVector();
        World world = player.getWorld();

        // Add immovable blocks to a list for exclusion
        List<Material> immovableBlocks = List.of(
                Material.BEDROCK, Material.SPAWNER, Material.OBSIDIAN, Material.LAVA, Material.WATER,
                Material.REINFORCED_DEEPSLATE, Material.RED_BED, Material.BLACK_BED, Material.BLUE_BED,
                Material.BROWN_BED, Material.CYAN_BED, Material.GRAY_BED, Material.GREEN_BED, Material.LIGHT_BLUE_BED,
                Material.LIGHT_GRAY_BED, Material.LIME_BED, Material.MAGENTA_BED, Material.ORANGE_BED, Material.PINK_BED,
                Material.PURPLE_BED, Material.WHITE_BED, Material.YELLOW_BED, Material.ACACIA_DOOR, Material.BIRCH_DOOR,
                Material.CRIMSON_DOOR, Material.DARK_OAK_DOOR, Material.IRON_DOOR, Material.JUNGLE_DOOR, Material.OAK_DOOR,
                Material.SPRUCE_DOOR, Material.WARPED_DOOR, Material.CHEST, Material.ENDER_CHEST, Material.TRAPPED_CHEST,
                Material.BARREL, Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX,
                Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
                Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
                Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX,
                Material.YELLOW_SHULKER_BOX
        );

        // Block directly under the player
        Block blockUnderPlayer = player.getLocation().subtract(0, 1, 0).getBlock();

        for (double radius = 1; radius <= maxRadius; radius += 1) {
            double finalRadius = radius;

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (double angle = 0; angle < 360; angle += 10) {
                        double radians = Math.toRadians(angle);
                        double x = Math.cos(radians) * finalRadius;
                        double z = Math.sin(radians) * finalRadius;
                        Block block = world.getBlockAt(origin.getBlockX() + (int) x, origin.getBlockY() - 1, origin.getBlockZ() + (int) z);

                        // Check if the block is solid and not immovable or the block under the player
                        if (block.getType().isSolid() && !immovableBlocks.contains(block.getType()) && !block.equals(blockUnderPlayer)) {
                            Material originalMaterial = block.getType();
                            BlockData originalBlockData = block.getBlockData(); // Store the block data to maintain orientation
                            Vector originalLocation = block.getLocation().toVector();

                            // Create a falling block to simulate the bounce
                            FallingBlock fallingBlock = world.spawnFallingBlock(block.getLocation().add(0.5, 0.5, 0.5), originalBlockData);
                            fallingBlock.setDropItem(false); // Ensure the block doesn't drop as an item
                            fallingBlock.setHurtEntities(false); // Prevent it from causing damage

                            // Track the falling block
                            trackedFallingBlocks.add(fallingBlock);

                            // Remove the block
                            block.setType(Material.AIR);

                            // Apply velocity to the falling block to make it bounce
                            Vector velocity = new Vector(0, 0.5, 0); // Adjust velocity for bounce height
                            fallingBlock.setVelocity(velocity);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    // If the falling block is still around, remove it to avoid ghost blocks
                                    if (!fallingBlock.isDead()) {
                                        fallingBlock.remove();
                                    }

                                    // Reset the block to its original material and data
                                    Block resetBlock = world.getBlockAt(originalLocation.toLocation(world));
                                    resetBlock.setBlockData(originalBlockData);

                                    // Stop tracking the falling block
                                    trackedFallingBlocks.remove(fallingBlock);
                                }
                            }.runTaskLater(CUU.getInstance(), 30L); // Reset the block after 1.5 seconds (30 ticks)
                        }
                    }
                }
            }.runTaskLater(CUU.getInstance(), (long) (delayBetweenWaves * radius));
        }
    }

    @EventHandler
    public void onFallingBlockChange(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            if (trackedFallingBlocks.contains(fallingBlock)) {
                event.setCancelled(true); // Prevent the block change
            }
        }
    }

    private boolean isOnCooldown(Player p) {
        return cooldown.containsKey(p.getUniqueId()) && System.currentTimeMillis() - cooldown.get(p.getUniqueId()) < COOLDOWN_TIME;
    }

    private static void cancelCooldown(Player p) {
        cooldown.remove(p.getUniqueId());
    }

    private void setCooldown(Player p) {
        cooldown.put(p.getUniqueId(), System.currentTimeMillis());
    }

    public static void showCooldown(Player p, String title, int seconds) {
        BossBar bossBar = Bukkit.createBossBar("shield_cooldown", BarColor.BLUE, BarStyle.SEGMENTED_20);
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
                    cancelCooldown(p);
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

    public static void cancelTasks() {
        for (BukkitTask task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }
        tasks.clear();
    }

}
