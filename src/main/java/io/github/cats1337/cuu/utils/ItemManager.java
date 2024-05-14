package io.github.cats1337.cuu.utils;

import io.github.cats1337.cuu.CUU;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class ItemManager {
    protected static final CUU plugin = CUU.getInstance();
    static FileConfiguration config = CUU.getInstance().getConfig();

    private static final ItemStack[] crownItems = new ItemStack[]{ new ItemStack(Material.SCULK_CATALYST, 4), new ItemStack(Material.MUSIC_DISC_OTHERSIDE, 2), new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2), new ItemStack(Material.NETHERITE_HELMET, 1) };
    private static final ItemStack[] chestItems = new ItemStack[]{ new ItemStack(Material.SHULKER_SHELL, 4), new ItemStack(Material.END_CRYSTAL, 2), new ItemStack(Material.DRAGON_BREATH, 2), new ItemStack(Material.NETHERITE_CHESTPLATE, 1) };
    private static final ItemStack[] legsItems = new ItemStack[]{ new ItemStack(Material.MAGMA_CREAM, 4), new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2), new ItemStack(Material.WITHER_SKELETON_SKULL, 2), new ItemStack(Material.NETHERITE_LEGGINGS, 1) };
    private static final ItemStack[] bootsItems = new ItemStack[]{ new ItemStack(Material.RABBIT_FOOT, 4), new ItemStack(Material.TOTEM_OF_UNDYING, 2), new ItemStack(Material.TURTLE_HELMET, 2), new ItemStack(Material.NETHERITE_BOOTS, 1) };
    private static final ItemStack[] bowItems = new ItemStack[]{ new ItemStack(Material.ECHO_SHARD, 4), new ItemStack(Material.ENDER_EYE, 2), new ItemStack(Material.END_CRYSTAL, 2), new ItemStack(Material.BOW, 1) };
    private static final ItemStack[] axeItems = new ItemStack[]{ new ItemStack(Material.WITHER_ROSE, 4), new ItemStack(Material.WITHER_SKELETON_SKULL, 4), new ItemStack(Material.NETHERITE_AXE, 1) };
    private static final ItemStack[] pickItems = new ItemStack[]{ new ItemStack(Material.GUNPOWDER, 128), new ItemStack(Material.SUGAR, 128), new ItemStack(Material.SCULK_CATALYST, 4), new ItemStack(Material.NETHERITE_PICKAXE, 1) };


    // return recipe items
    public static void refundMats(String itemName, Player p) {
        // give the player back the items
        switch (itemName) {
            case "DOOM_CROWN":
                for (ItemStack item : crownItems) {
                    p.getInventory().addItem(item);
                }
                break;
            case "DOOM_CHESTPLATE":
                for (ItemStack item : chestItems) {
                    p.getInventory().addItem(item);
                }
                break;
            case "DOOM_LEGGINGS":
                for (ItemStack item : legsItems) {
                    p.getInventory().addItem(item);
                }
                break;
            case "DOOM_BOOTS":
                for (ItemStack item : bootsItems) {
                    p.getInventory().addItem(item);
                }
                break;
            case "DOOM_BOW":
                for (ItemStack item : bowItems) {
                    p.getInventory().addItem(item);
                }
                break;
            case "DOOM_AXE":
                for (ItemStack item : axeItems) {
                    p.getInventory().addItem(item);
                }
                break;
            case "DOOM_PICKAXE":
                for (ItemStack item : pickItems) {
                    p.getInventory().addItem(item);
                }
                break;
        }
    }


    // getCrafted - check if the item has been crafted
    public static boolean getCrafted(String itemName) {
        String key = "items." + itemName + ".crafted";
        return plugin.getConfig().getBoolean(key);
    }

    // setCrafted - set the crafted status of an item
    public static void setCrafted(String itemName, boolean crafted) {
        String key = "items." + itemName + ".crafted";
        plugin.getConfig().set(key, crafted);
        plugin.saveConfig();
    }

    // getExists - check if the item exists
    public static boolean getExists(String itemName) {
        String key = "items." + itemName + ".exists";
        return plugin.getConfig().getBoolean(key);
    }

    // setExists - set the existence status of an item
    public static void setExists(String itemName, boolean exists) {
        String key = "items." + itemName + ".exists";
        plugin.getConfig().set(key, exists);
        plugin.saveConfig();
    }

    // getItemOwner - get the owner of an item
    public static String getItemOwner(String itemName) {
        String key = "items." + itemName + ".owner";
        return plugin.getConfig().getString(key);
    }

    // setItemOwner - set the owner of an item
    public static void setItemOwner(String itemName, Player p) {
        String key = "items." + itemName + ".owner";
        // use username instead of UUID
        plugin.getConfig().set(key, p.getName());
        plugin.saveConfig();
//        call the passive effect
        Passive.givePassiveEffect(p, itemName);
    }

    // removeItemOwner - remove the owner of an item
    public static void removeItemOwner(String itemName) {
        String key = "items." + itemName + ".owner";
        plugin.getConfig().set(key, null);
        plugin.saveConfig();
    }

    // getRitualActive - get if a ritual is active
    public static boolean getRitualActive() {
        String key = "variables.ritualActive";
        return plugin.getConfig().getBoolean(key);
    }

    // setRitualActive - set if a ritual is active
    public static void setRitualActive(boolean active) {
        String key = "variables.ritualActive";
        plugin.getConfig().set(key, active);
        plugin.saveConfig();
    }

    // getItems - get all items from the config, replacing _ with space, uppercase first letterm lowercase rest
    public static String[] getItems() {
        Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false).toArray(new String[0]);
        String[] items = new String[Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false).size()];
        int i = 0;
        for (String item : Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false)) {
            items[i] = NameCheck.convertToDisplayName(item);
            i++;
        }

        return items;
    }

    // getConfigItems - get all items from the config
    // returns an array of item names in format of "DOOM_PICKAXE" etc
    public static String[] getConfigItems() {
        // items.NAME
        String[] items = new String[Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false).size()];
        int i = 0;
        for (String item : Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false)) {
            items[i] = item;
            i++;
        }
        return items;
    }

    public static String[] getConfigItemsString() {
        // items.NAME
        String[] items = new String[Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false).size()];
        int i = 0;
        for (String item : Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false)) {
            items[i] = NameCheck.convertToConfigName(item);
            i++;
        }
        return items;
    }

    // check if item is in config
    public static boolean checkItemInConfig(String itemName) {
        return Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false).contains(itemName);
    }

    // checkOwnItem - check if the player owns any items
    public static boolean checkOwnItem(Player p) {
        for (String item : getConfigItems()) {
            if (p.getName().equals(getItemOwner(item))) {
                return true;
            }
        }
        return false;
    }

    // getOwnedItems - get all items owned by the player
    public static String[] getOwnedItems(Player p) {
        String[] items = new String[getConfigItems().length];
        int i = 0;
        for (String item : getConfigItems()) {
            if (p.getName().equals(getItemOwner(item))) {
                items[i] = item;
                i++;
            }
        }
        items = Arrays.stream(items).filter(Objects::nonNull).toArray(String[]::new);
        return items;
    }

    // get ritualTime
    public static int getRitualTime(String type) {
        if (type.equals("dragon")){
            return (config.getInt("variables.dragonRitualTime") * 60);
        } else {
            return (config.getInt("variables.ritualTime") * 60);
        }
    }

    public static int getConfigInt(String var){
        return (config.getInt("variables." + var));
    }
}
