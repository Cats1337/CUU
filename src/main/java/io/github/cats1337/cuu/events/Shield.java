package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.utils.NameCheck;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Shield implements Listener {
    private final HashMap<UUID, Long> cooldown;
    public Shield() {this.cooldown = new HashMap<>();}
    private static final long COOLDOWN_TIME = 30 * 1000; // 30 seconds
    private static final int CHARGE_TIME = 5; // 5 seconds
    private static final List<BukkitTask> tasks = new ArrayList<>();
//    static BossBar bossBar = Bukkit.createBossBar("doombow_cooldown", BarColor.BLUE, BarStyle.SEGMENTED_20);


    // on shield break (10% chance to launch attacker into the sky)
    @EventHandler
    private void onShieldBreak(EntityDamageByEntityEvent e) {
        // check if the entity being attacked is a player
        if (!(e.getEntity() instanceof Player attacker)) return; // if not, ignore

        String itemName = NameCheck.extractItemName(attacker.getInventory().getItemInMainHand());
        // if not in mainhand, check offhand
        if (!itemName.equals("Doom Shield")) {
            itemName = NameCheck.extractItemName(attacker.getInventory().getItemInOffHand());
        } else {
            return;
        }

//        EntityEffect.SHIELD_BREAK


        // Check if the player is holding the Doom Shield using the NameCheck helper
        if (itemName.equals("Doom Shield")) {
            if (Math.random() < 0.1) { // 10% chance
                attacker.setVelocity(attacker.getLocation().getDirection().multiply(3).setY(1)); // Launch the attacker into the sky
            }
        }
    }

    @EventHandler
    public void playerInteractAir(PlayerInteractEvent e) {
//        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
//            EquipmentSlot hand = e.getHand();
//            ItemStack handItem = null;
//            if (hand == EquipmentSlot.HAND) {
//                handItem = e.getPlayer().getInventory().getItemInMainHand();
//            } else if (hand == EquipmentSlot.OFF_HAND) {
//                handItem = e.getPlayer().getInventory().getItemInOffHand();
//            }
//            if (handItem.getType() == Material.SHIELD) {
//                e.setCancelled(true);
//            }
//        }

        // Doom Shield:
        // Track how long the player has the shield blocked
        // Bar that charges up when blocking, fills up in 5? seconds
        // When the bar is full, if player stops blocking, send a shockwave in the direction they were facing, launching all entities into the air
        // Applies slowness 2 for 3 seconds to shield user
        // Shockwave has a range of 7 blocks
        // Shockwave has a cooldown of 30 seconds
        // Shockwave applies blindness 3 for 5 seconds, weakness 3 for 5 seconds

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (e.getHand() == EquipmentSlot.HAND){
                ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
                if(e.getHand() == EquipmentSlot.OFF_HAND){
                    item = e.getPlayer().getInventory().getItemInOffHand();
                }
                if (item.getType() == Material.SHIELD && NameCheck.extractItemName(item).equals("Doom Shield")) {
                    // Start charging the shield bar
                    // Apply slowness 2 for 5 seconds
                    // When the bar is full, play sound, when released, launch all entities in a 7 block radius into the air, shockwave

                    // ActionBar message: "Charging Shield"■□□□□
                    // ActionBar message: "Charging Shield"■■□□□ etc.
                    String actionMessage = "§eCharging Shield: §c□□□□□ §b5s";
                    String p = e.getPlayer().getName();
                    e.getPlayer().sendActionBar(actionMessage);

                }
            }
        }

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
