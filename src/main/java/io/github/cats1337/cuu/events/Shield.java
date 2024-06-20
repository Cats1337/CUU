package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.utils.ItemManager;
import io.github.cats1337.cuu.utils.NameCheck;
import net.kyori.adventure.text.Component;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Shield implements Listener {
    private final HashMap<UUID, Long> cooldown;
    private final List<UUID> charged = new ArrayList<>();
    public Shield() {this.cooldown = new HashMap<>();}
    private static final long COOLDOWN_TIME = ItemManager.getConfigInt("shieldCooldown") * 1000L; // 30 seconds
    private static final int CHARGE_TIME = 5; // 5 seconds
    private static final List<BukkitTask> tasks = new ArrayList<>();
//    static BossBar bossBar = Bukkit.createBossBar("doombow_cooldown", BarColor.BLUE, BarStyle.SEGMENTED_20);


    // on shield break (10% chance to launch attacker into the sky)

    @EventHandler
    public void onShieldBreak(EntityDamageByEntityEvent e) {
        // This triggers anytime, not just when a shield is broken...
        // when shield breaks:

//        EntityEffect.SHIELD_BREAK

        // if shield is broken
        if (!(e.getEntity() instanceof Player p)) return;

        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        ItemStack offHandItem = p.getInventory().getItemInOffHand();

        if (isDoomShield(mainHandItem) || isDoomShield(offHandItem)) {
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
        }

        // when stop blocking (shield)

    }

    private boolean isDoomShield(ItemStack item) {
        return item != null && item.getType() == Material.SHIELD && NameCheck.extractItemName(item).equals("Doom Shield");
    }

    private void startChargingShield(Player p) {
        if (isOnCooldown(p)) {
            p.sendMessage("§cDoom Shield is still on cooldown!");
            return;
        }

        showChargingProgress(p);

        BukkitTask shieldCharge = new BukkitRunnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                ticksElapsed++;

                if (ticksElapsed >= CHARGE_TIME * 20) {
//                    launchShockwave(p, shield);
                    charged.add(p.getUniqueId());
                    cancelCooldown(p);
                    this.cancel();
                }
            }
        }.runTaskTimer(CUU.getInstance(), 0, 1);
        tasks.add(shieldCharge);
    }

    private void showChargingProgress(Player player) {
        BukkitTask shieldProg = new BukkitRunnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                ticksElapsed++;

                if (ticksElapsed <= CHARGE_TIME * 20) {
                    int progress = ticksElapsed / (CHARGE_TIME * 20 / 5); // 5 segments
                    player.sendActionBar(Component.text("§eCharging Shield: §c" + getProgressBar(progress) + " §b" + (CHARGE_TIME - ticksElapsed / 20) + "s"));
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(CUU.getInstance(), 0, 1);
        tasks.add(shieldProg);
    }

    private String getProgressBar(int progress) {
        // §7■■■■■
        // §c■§7■■■■
        // §c■§c■§7■■■
        // §e■§e■§e■§7■■
        // §e■§e■§e■§e■§7■
        // §a■■■■■

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
        charged.remove(p.getUniqueId());

        p.playSound(p.getLocation(), "entity.lightning_bolt.impact", 2F, .3F);

        Location shockwaveOrigin = p.getLocation().add(p.getLocation().getDirection());
        double range = 7.0;

        for (Entity entity : p.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(5*20, 2));
                livingEntity.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(5*20, 1));

                entity.setVelocity(entity.getLocation().toVector().subtract(shockwaveOrigin.toVector()).normalize().multiply(1.5));
            }
        }

        cancelCooldown(p);
        p.sendActionBar(Component.empty());
        p.removePotionEffect(PotionEffectType.SLOW);
    }

    private boolean isOnCooldown(Player p) {
        return cooldown.containsKey(p.getUniqueId()) && System.currentTimeMillis() - cooldown.get(p.getUniqueId()) < COOLDOWN_TIME;
    }

    private void cancelCooldown(Player p) {
        cooldown.remove(p.getUniqueId());
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
