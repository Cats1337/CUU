package io.github.cats1337.cuu.events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.utils.NameCheck;
import io.github.cats1337.cuu.utils.Passive;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import thirtyvirus.uber.helpers.Utilities;

import java.util.HashMap;
import java.util.UUID;

public class Consumption implements Listener {

    private final HashMap<UUID, Long> cooldown;

    public Consumption() {
        this.cooldown = new HashMap<>();
    }

    @EventHandler
    private void consumeItem(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        // extract item name
        String itemName = NameCheck.extractItemName(e.getItem());
        ItemStack consumItem = e.getItem();

        if (consumItem.getType() == Material.MILK_BUCKET && (!itemName.equals("Bucket Of Golden Carrots"))) {
            Passive.returnPassiveEffects(p);
        }

        // check if item is an UberItem, if not, ignore
        if(!NameCheck.isUber(itemName)) { return; }

//        Netherite Apple
//          Like a Golden Apple but gives
//          Resistance 1 for 10 Seconds
//          Regeneration 3 for 5 Seconds
//          Absorption 3 for 60 Seconds
//          Fire Resistance for 60 Seconds
        if(itemName.equals("Netherite Apple")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1200, 0));

            // Golden Apple saturation and hunger
            p.setSaturation(Math.min(p.getSaturation() + 9.6f, 20));
            p.setFoodLevel(Math.min(p.getFoodLevel() + 4, 20));
            reduceItem(p, consumItem);
        }

//    Bucket Of Golden Carrots
//      Re-usable Food Item
//      Grants full hunger and saturation
//      Cooldown (20 Seconds)

        if(itemName.equals("Bucket Of Golden Carrots")) {
            if (!cooldown.containsKey(p.getUniqueId()) || System.currentTimeMillis() - cooldown.get(p.getUniqueId()) > 20000) { // 20 seconds
                cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                p.setSaturation(20);
                p.setFoodLevel(20);

                Passive.returnPassiveEffects(p);

                // make a clone/copy of consumItem
                ItemStack cdBucket = consumItem.clone();
                // change the item type to a bucket
//                cdBucket.setType(Material.BUCKET);
                // set the custom model data
                Utilities.setCustomModelData(cdBucket, 1338);

                if(p.getInventory().getItemInMainHand().equals(consumItem)) {
                    p.getInventory().setItem(p.getInventory().getHeldItemSlot(), cdBucket);
                } else if(p.getInventory().getItemInOffHand().equals(consumItem)) {
                    p.getInventory().setItemInOffHand(cdBucket);
                }

                // replace item with empty bucket while cooldown is active
                Bukkit.getScheduler().runTaskLater(CUU.getInstance(), () -> {
                    // replace cdBucket with consumItem whereever it is in the player's inventory
                    if(p.getInventory().contains(cdBucket)) {
                        // set cdBucket to consumItem
                        p.getInventory().setItem(p.getInventory().first(cdBucket), consumItem);
                        Utilities.setCustomModelData(consumItem, 1337);
                    }
                }, 20 * 20L);


            } else {
                // get remaining time on cooldown
                long timeLeft = 20 - (System.currentTimeMillis() - cooldown.get(p.getUniqueId())) / 1000;
                Text.of("§cYou must wait " + timeLeft + " seconds before using this item again!").send(p);
                Passive.returnPassiveEffects(p);
                e.setCancelled(true);
            }
        }
    }

    public static void reduceItem(Player p, ItemStack item) {
        if(p.getInventory().getItemInMainHand().equals(item)) {
            item.setAmount(item.getAmount() - 1);
            p.getInventory().setItem(p.getInventory().getHeldItemSlot(), item);
        } else if(p.getInventory().getItemInOffHand().equals(item)) {
            item.setAmount(item.getAmount() - 1);
            p.getInventory().setItemInOffHand(item);
        }
    }
}
