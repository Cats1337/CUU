package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.utils.NameCheck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import thirtyvirus.uber.UberItems;
import thirtyvirus.uber.helpers.Utilities;

import java.util.HashMap;
import java.util.UUID;

public class Projectile implements Listener {

    private final HashMap<UUID, Long> cooldown;
    private static final Logger logger = LogManager.getLogger(DragonSword.class);
    public Projectile() {
        this.cooldown = new HashMap<>();
    }

    @EventHandler
    private void onDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() == EntityType.ENDER_PEARL) {
            if (Utilities.getEntityTag(event.getDamager(), "infini").equals("a")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
//    on bow shoot
    private void onBowShoot(EntityShootBowEvent e) {
        // Check if the bow is doom bow
        if (e.getBow() != null && e.getBow().hasItemMeta() && NameCheck.checkName(e.getBow())) {
            // Check if the entity shooting the bow is a player
            if (e.getEntity() instanceof Player p) {
                if (!cooldown.containsKey(p.getUniqueId()) || System.currentTimeMillis() - cooldown.get(p.getUniqueId()) > 30000) { // 30 seconds
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                    // Apply cooldown
                    // Give slowness 4 for 5 seconds
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 3));
                    // Show boss bar in a separate thread to avoid blocking the main thread
                    new Thread(() -> {
                        showBossBar(p, 30);
                    }).start();
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }
    public void showBossBar(Player player, int seconds) {
        BossBar bossBar = Bukkit.createBossBar("Cooldown", BarColor.RED, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);
//               set bossbar to show countdown from 30 seconds to 0
        Bukkit.getScheduler().runTaskAsynchronously(UberItems.getInstance(), () -> {
            for (int i = seconds; i >= 0; i--) {
                bossBar.setTitle("Cooldown: " + i);
                //                    reduce bossbar every second
                bossBar.setProgress((double) i / seconds);

                if (i >= 10) {
                    bossBar.setColor(BarColor.YELLOW);
                } if (i >= 20) {
                    bossBar.setColor(BarColor.RED);
                } else {
                    bossBar.setColor(BarColor.GREEN);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted exception", e);
                }
            } // remove bossbar after countdown is done
            bossBar.removePlayer(player);
            player.playSound(player.getLocation(), "minecraft:block.note_block.chime", 1, 1);
        });
    }

}
