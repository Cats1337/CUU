package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.utils.ItemManager;
import io.github.cats1337.cuu.utils.NameCheck;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Projectile implements Listener {

    private final HashMap<UUID, Long> cooldown;
    public Projectile() {
        this.cooldown = new HashMap<>();
    }
    private static final List<BukkitTask> tasks = new ArrayList<>();
    static BossBar bossBar = Bukkit.createBossBar("doombow_cooldown", BarColor.BLUE, BarStyle.SEGMENTED_20);

    @EventHandler
    private void onBowShoot(EntityShootBowEvent e) {
        int cd = ItemManager.getConfigInt("doombowCooldown");
        // Check if the bow is doom bow
        if (e.getBow() != null && e.getBow().hasItemMeta() && NameCheck.checkName(e.getBow())) {
            // Check if the entity shooting the bow is a player
            if (e.getEntity() instanceof Player p) {
                if (!cooldown.containsKey(p.getUniqueId()) || System.currentTimeMillis() - cooldown.get(p.getUniqueId()) > (cd * 1000L)) { // 20 seconds
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                    // Apply cooldown
                    p.playSound(p.getLocation(), "block.respawn_anchor.deplete", 2F, 2F);
                    // Give slowness 4 for 10 seconds
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 3));

                    e.setCancelled(true);

                    // Launch WitherSkull projectile
                    WitherSkull skull = e.getEntity().launchProjectile(WitherSkull.class);
                    skull.setVelocity(e.getProjectile().getVelocity());

                    skull.addScoreboardTag("DOOM_BOW_PROJECTILE");

                    // Spawn sonic boom particles that follow the projectile
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (skull.isValid() && !skull.isDead()) {
                                // Calculate particle spawn location
                                Location particleLocation = skull.getLocation().add(skull.getVelocity().normalize().multiply(0.5));

                                // Spawn sonic boom particles
                                p.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLocation, 5, 0.1, 0.1, 0.1, 0);
                            } else {
                                cancel();
                            }
                        }
                    }.runTaskTimer(CUU.getInstance(), 0L, 2L); // Adjust interval for smoother effect

                    // Show boss bar in a separate thread to avoid blocking the main thread
                    new Thread(() -> {
                        showBossBar(p, "Cooldown", cd);
                    }).start();
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }


    // Check if the entity hit is a player using the doom bow

    @EventHandler
    private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof WitherSkull skull) {
            if (skull.getScoreboardTags().contains("DOOM_BOW_PROJECTILE")) {
                if (e.getEntity() instanceof Player p) {
                    // Apply wither 3 for 6 seconds
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 3 * 20, 3));
                    // Apply poison 3 for 6 seconds
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 3 * 20, 3));
                    // Pierce shield
                    p.damage(7); // 1 = 0.5 heart
                }
            }
        }
    }

    public static void showBossBar(Player p, String title, int seconds) {
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

    public static void cancelTasks() {
        for (BukkitTask task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }
        tasks.clear();
    }


}
