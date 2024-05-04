package io.github.cats1337.cuu.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Passive {
    public static void givePassiveEffect(Player p, String itemName){
        if(!NameCheck.isUber(itemName)) { return; }

        String item = NameCheck.convertToConfigName(itemName);

        switch (item) {
            case "DOOM_CROWN":
                p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, -1, 2));
                break;
            case "DOOM_CHESTPLATE":
                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, -1, 1));
                break;
            case "DOOM_LEGGINGS":
                p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, -1, 0));
                break;
            case "DOOM_BOOTS":
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 0));
                break;
            case "DOOM_SWORD":
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, -1, 0));
                break;
            case "DOOM_PICKAXE":
                p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, -1, 1));
                break;
        }
    }
    public static void removePassiveEffect(Player p, String itemName){
        if(!NameCheck.isUber(itemName)) { return; } // if the item is not an UberItem, ignore it

        String item = NameCheck.convertToConfigName(itemName);

        switch (item) {
            case "DOOM_CROWN":
                p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
                break;
            case "DOOM_CHESTPLATE":
                p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                break;
            case "DOOM_LEGGINGS":
                p.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                break;
            case "DOOM_BOOTS":
                p.removePotionEffect(PotionEffectType.SPEED);
                break;
            case "DOOM_SWORD":
                p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                break;
            case "DOOM_PICKAXE":
                p.removePotionEffect(PotionEffectType.FAST_DIGGING);
                break;
        }
    }

    // returnPassvieEffects (Player p)
    // give player all passive effects they have, but don't because they drank milk...

    public static void returnPassiveEffects(Player p){
        if( p != null){
            String[] owed = ItemManager.getOwnedItems(p);
            for (String item : owed) {
                givePassiveEffect(p, item);
            }
        }
    }

}
