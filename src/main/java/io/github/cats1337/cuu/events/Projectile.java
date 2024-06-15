package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.utils.ItemManager;
import io.github.cats1337.cuu.utils.NameCheck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
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
    private static final Logger logger = LogManager.getLogger(DragonSword.class);
    public Projectile() {
        this.cooldown = new HashMap<>();
    }
    private static final List<BukkitTask> tasks = new ArrayList<>();
    static BossBar bossBar = Bukkit.createBossBar("doombow_cooldown", BarColor.BLUE, BarStyle.SEGMENTED_20);

    @EventHandler
//    on bow shoot
    private void onBowShoot(EntityShootBowEvent e) {
        int cd = ItemManager.getConfigInt("doombowCooldown");
        // Check if the bow is doom bow
        if (e.getBow() != null && e.getBow().hasItemMeta() && NameCheck.checkName(e.getBow())) {
            // Check if the entity shooting the bow is a player
            if (e.getEntity() instanceof Player p) {
                if (!cooldown.containsKey(p.getUniqueId()) || System.currentTimeMillis() - cooldown.get(p.getUniqueId()) > (cd * 1000L)) { // 20 seconds
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                    // Apply cooldown
                    // Give slowness 4 for 10 seconds
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 3));

                    e.setCancelled(true);

                    // Warden sonic boom particles in direction of arrow
                    p.getWorld().spawnParticle(Particle.SONIC_BOOM, e.getProjectile().getLocation(), 25, 0.5, 0.5, 0.5, 0.1);

                    WitherSkull skull = e.getEntity().launchProjectile(WitherSkull.class);
                    skull.setVelocity(e.getProjectile().getVelocity());

                    skull.addScoreboardTag("DOOM_BOW_PROJECTILE");
                    skull.getScoreboardTags();
                    skull.getScoreboardTags().add("DOOM_BOW_PROJECTILE");
                    skull.getScoreboardTags();


                    // Show boss bar in a separate thread to avoid blocking the main thread
                    new Thread(() -> {
                        showBossBar(p,"Cooldown", cd);

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
                    p.damage(1);
                }
            }
        }
    }


            // check if the entity hit is a player
            // if player, apply posion 3 for 6 seconds
            // check if player has shield, if so, apply wither 3 for 6 seconds, and pierce shield (apply damage to player)
            // poison 3 arrow

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


}
