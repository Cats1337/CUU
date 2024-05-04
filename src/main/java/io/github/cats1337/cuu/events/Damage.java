package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.CUU;
import io.github.cats1337.cuu.utils.NameCheck;
import io.github.cats1337.cuu.utils.ItemManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Damage implements Listener {

    static FileConfiguration config = CUU.getInstance().getConfig();

    static double lifeSteal = config.getDouble("variables.lifesteal") / 100; // 15 = 0.15 = 15%

    @EventHandler
    // When player damages another entity, steal #% of the damage dealt as health
    private void onDamageEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return; // Check if the damager is a player

        String itemName = NameCheck.extractItemName(p.getInventory().getItemInMainHand());

        // Check if the player is holding the Doom Sword using the NameCheck helper
        // check if the player is the owner of the item
        if (itemName.equals("Doom Axe") && p.getName().equals(ItemManager.getItemOwner("Doom Sword"))){
            double steal = e.getDamage() * lifeSteal; // Calculate the amount of health to steal
            p.setHealth(Math.min(p.getHealth() + steal, p.getMaxHealth()));
        }

    }
}
