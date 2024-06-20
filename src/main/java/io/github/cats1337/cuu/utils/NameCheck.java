package io.github.cats1337.cuu.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import thirtyvirus.uber.UberItems;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NameCheck {

    /**
     * Extract the item name and check if it's an UberItem
     * @param item The item to check
     * @return True if the item is an UberItem, false otherwise
     */
    public static boolean checkName(ItemStack item) {
        String itemName = extractItemName(item);
        return itemName != null && isUber(itemName);
    }

    /**
     * Check if the provided item name is an UberItem
     * @param itemName The name of the item
     * @return True if the item is an UberItem, false otherwise
     */
    public static boolean isUber(String itemName) {
        Collection<String> uberItemNames = UberItems.getItemNames();

        // Doom Bow -> doom_bow
        itemName = itemName.toLowerCase().replace(" ", "_");

        // Check if the provided name matches any UberItem name
        for (String uberItemName : uberItemNames) {
            if (uberItemName.equalsIgnoreCase(itemName)) {
                return true;
            }
        }
        return false;
    }

    // isUber but itemStack
    public static boolean isUberItem(ItemStack item) {
        String itemName = extractItemName(item);
        return isUber(itemName);
    }

    public static String convertToConfigName(String itemName){
        // Doom Bow -> DOOM_BOW
        itemName = itemName.toUpperCase().replace(" ", "_");
        return itemName;
    }

    public static String convertToConfigNameItem(ItemStack item){
        String itemName = extractItemName(item);
        return convertToConfigName(itemName);
    }

    /**
     * Extract the item name from the item's display name
     * @param item The item to extract the name from
     * @return The extracted item name
     */
    // Extract item name from item display name (ie. "tag: {display: {Name: ... "text":"Doom Axe"}] -> "Doom Axe")
    public static String extractItemName(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                TextComponent werp = (TextComponent) meta.displayName();

                if (werp != null) {
                    String nirp = null;
                    for (Component blort : werp.children()) {
                        if (blort instanceof TextComponent) {
                            nirp = ((TextComponent) blort).content();
                            break; // Break the loop once the name is found
                        }
                    }
                    return nirp;
                }
            }
        }
        if (item == null) {
            return "";
        }
        if (item.getItemMeta() != null) {
            return item.getItemMeta().getDisplayName();
        }
        return String.valueOf(item.displayName());
    }

    /**
     * Convert the item name to a display name
     * @param itemName The item name to convert
     * @return The converted display name
     */
    // Convert item name to display name, ie. "DOOM_PICKAXE" -> "Doom Pickaxe"
    public static String convertToDisplayName(String itemName) {
        String[] words = itemName.split("[_\\s]+"); // Split the item name by underscores and spaces
        StringBuilder displayName = new StringBuilder();

        for (String word : words) {
            // Convert the first letter of each word to uppercase and the rest to lowercase
            String formattedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            displayName.append(formattedWord).append(" ");
        }
        // Trim any extra whitespace and return the formatted display name
        return displayName.toString().trim();
    }

    // Extract item name, and count of items, ie. "DOOM_PICKAXE" -> "Doom Pickaxe, 2"
    public static Map<String, Integer> extractInvNameCount(Player p) {
        // Get the total amount of each item in the player's inventory
        ItemStack[] inv = p.getInventory().getContents();

        // Create a map to store item names and their counts
        Map<String, Integer> itemCountMap = new HashMap<>();

        // Count the occurrences of each item in the inventory
        for (ItemStack item : inv) {
            if (item == null) continue;

            // Extract the name of the item
            String itemName = extractItemName(item);

            // Update the count for the item in the map
            itemCountMap.put(itemName, itemCountMap.getOrDefault(itemName, 0) + item.getAmount());
        }

        return itemCountMap;
    }

}
