package io.github.cats1337.cuu.events;

import io.github.cats1337.cuu.utils.NameCheck;
import io.github.cats1337.cuu.utils.Passive;
import io.github.cats1337.cuu.utils.ItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Invent implements Listener {

//   when removing item from inventory, check if it's an UberItem, if it is, remove passive effect
//   drop the item on the ground, move to chest, picks up out of inventory, etc. if it's not in the inventory, remove passive effect

    @EventHandler
    private void inventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();



        // Check inventory for 'Healing Artifact' and 'Doom Potion' items
        // make sure the player only has 16 Healing Artifacts and 2 Doom Potions max. drop excess items
        for (Map.Entry<String, Integer> entry : NameCheck.extractInvNameCount(p).entrySet()) {
            String itemName = entry.getKey();
            int itemCount = entry.getValue();

            if (itemName.equals("Healing Artifact") && itemCount > 16) {
                int excessAmount = itemCount - 16;
                removeExcessItems(p, itemName, excessAmount);
            }

            if (itemName.equals("Doom Potion") && itemCount > 2) {
                int excessAmount = itemCount - 2;
                removeExcessItems(p, itemName, excessAmount);
            }
        }

//        --------={ Passive Effects }=--------

        if(!ItemManager.checkOwnItem(p)) { return; } // if player doesn't own any items, ignore

        String[] ownedItems = ItemManager.getOwnedItems(p);

        // Convert owned item names to display names
        String[] itemNames = Arrays.stream(ownedItems)
                .map(NameCheck::convertToDisplayName)
                .toArray(String[]::new);

        // If player doesn't own any items, ignore
        if (ownedItems.length == 0) { return; }

        // get contents of player's inventory, removing any null values
        ItemStack[] contents = Arrays.stream(p.getInventory().getContents())
                .filter(Objects::nonNull)
                .toArray(ItemStack[]::new);

        // Get display names of items in player's inventory
        String[] itemNamesInInventory = Arrays.stream(contents)
                .filter(NameCheck::checkName)
                .map(NameCheck::extractItemName)
                .toArray(String[]::new);

        // Iterate through owned items and check if they are not in the inventory
        for (String itemName : itemNames) {
            boolean found = false;
            for (String inventoryItemName : itemNamesInInventory) {
                if (itemName.equals(inventoryItemName)) {
                    Passive.givePassiveEffect(p, itemName);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Passive.removePassiveEffect(p, itemName);
            }
        }
    }

    // Method to remove excess items from the player's inventory
    private void removeExcessItems(Player p, String itemName, int excessAmount) {
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length && excessAmount > 0; i++) {
            ItemStack item = contents[i];
            if (item == null) continue;
            if (NameCheck.extractItemName(item).equals(itemName)) {
                if (item.getAmount() <= excessAmount) {
                    excessAmount -= item.getAmount();
                    // hotbar is slots: 0-8, inventory is slots: 9-35
                    // start at 35 and reverse to 0 to remove form the hotbar last
                    for (int j = 35; j >= 0; j--) {
                        if (Objects.requireNonNull(p.getInventory().getItem(j)).equals(item)) {
                            p.getInventory().setItem(j, null);
                            break;
                        }
                    }
                    // Drop the excess item on the ground
                    p.getWorld().dropItem(p.getLocation(), item);
                } else {
                    // Create a new ItemStack with the updated amount
                    ItemStack updatedItem = item.clone();
                    updatedItem.setAmount(item.getAmount() - excessAmount);
                    p.getInventory().setItem(i, updatedItem); // Update the stack amount

                    for (int k = 35; k >= 0; k--) {
                        if (Objects.requireNonNull(p.getInventory().getItem(k)).equals(item)) {
                            p.getInventory().setItem(k, updatedItem);
                            break;
                        }
                    }

                    // Drop the excess amount on the ground
                    ItemStack excessItem = item.clone();
                    excessItem.setAmount(excessAmount);
                    p.getWorld().dropItem(p.getLocation(), excessItem);
                    excessAmount = 0; // No more excess to remove
                }
            }
        }
    }


    @EventHandler
    private void onInventoryAction(InventoryClickEvent e) {

        if(!(e.getWhoClicked() instanceof Player p)) { return; } // if not player, ignore
        ItemStack item = e.getCurrentItem();
        if (item == null || !NameCheck.checkName(item)) { return; }

        if(
                e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                        e.getAction() == InventoryAction.PLACE_ALL ||
                        e.getAction() == InventoryAction.PLACE_ONE ||
                        e.getAction() == InventoryAction.PICKUP_HALF ||
                        e.getAction() == InventoryAction.PICKUP_ONE ||
                        e.getAction() == InventoryAction.PICKUP_ALL ||
                        e.getAction() == InventoryAction.HOTBAR_SWAP ||
                        e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD
        ) {
            if(!p.getInventory().contains(item)) {
                Passive.removePassiveEffect(p, NameCheck.extractItemName(item));
                ItemManager.removeItemOwner(NameCheck.extractItemName(item));
            } else {
                Passive.givePassiveEffect(p, NameCheck.extractItemName(item));
            }
        }

    }

    @EventHandler
    private void onPickups(PlayerAttemptPickupItemEvent e) {
        Player p = e.getPlayer();
        ItemStack pickedUpItem = e.getItem().getItemStack();

        if (!NameCheck.checkName(pickedUpItem)) { return; } // if the item is not an UberItem, ignore it

        String itemName = NameCheck.extractItemName(pickedUpItem);

        switch (itemName) {
            case "Doom Crown":
                e.setCancelled(true);
                e.getItem().remove();
                if (p.getInventory().getHelmet() != null) { // if player has a helmet equipped
                    if (p.getInventory().firstEmpty() == -1) { // if player has no space in inventory
                        p.getWorld().dropItem(p.getLocation(), p.getInventory().getHelmet()); // drop the helmet
                    } else {
                        p.getInventory().addItem(p.getInventory().getHelmet()); // add the helmet to the inventory
                    }
                }
                p.getInventory().setHelmet(pickedUpItem); // equip the Doom Crown
                p.playSound(p.getLocation(), "item.armor.equip_chain", 1, 1); // play the armor equip sound

                break;
            case "Doom Chestplate":
                e.setCancelled(true);
                e.getItem().remove();
                if (p.getInventory().getChestplate() != null) {
                    if (p.getInventory().firstEmpty() == -1) {
                        p.getWorld().dropItem(p.getLocation(), p.getInventory().getChestplate());
                    } else {
                        p.getInventory().addItem(p.getInventory().getChestplate());
                    }
                }
                p.getInventory().setChestplate(pickedUpItem);
                p.playSound(p.getLocation(), "item.armor.equip_chain", 1, 1);

                break;
            case "Doom Leggings":
                e.setCancelled(true);
                e.getItem().remove();
                if (p.getInventory().getLeggings() != null) {
                    if (p.getInventory().firstEmpty() == -1) {
                        p.getWorld().dropItem(p.getLocation(), p.getInventory().getLeggings());
                    } else {
                        p.getInventory().addItem(p.getInventory().getLeggings());
                    }
                }
                p.getInventory().setLeggings(pickedUpItem);
                p.playSound(p.getLocation(), "item.armor.equip_chain", 1, 1);

                break;
            case "Doom Boots":
                e.setCancelled(true);
                e.getItem().remove();
                if (p.getInventory().getBoots() != null) {
                    if (p.getInventory().firstEmpty() == -1) {
                        p.getWorld().dropItem(p.getLocation(), p.getInventory().getBoots());
                    } else {
                        p.getInventory().addItem(p.getInventory().getBoots());
                    }
                }
                p.getInventory().setBoots(pickedUpItem);
                p.playSound(p.getLocation(), "item.armor.equip_chain", 1, 1);

                break;

            case "Doom Sword":
            case "Doom Pickaxe":
            case "Doom Axe":
                Passive.givePassiveEffect(p, itemName);
                break;

            case "Doom Potion":
                int currentDoomPotionCount = NameCheck.extractInvNameCount(p).getOrDefault("Doom Potion", 0);
                int maxPotions = ItemManager.getConfigInt("maxPotions");
                int remainingDoomPotions = maxPotions - currentDoomPotionCount;
                int amountPot = pickedUpItem.getAmount();

                if (maxPotions >= -1){ break; } // if maxPotions is -1<, ignore

                if (currentDoomPotionCount == maxPotions) {
                    e.setCancelled(true); // Cancel pickup if max count reached
                    break;
                }

                if (currentDoomPotionCount >= maxPotions || currentDoomPotionCount + amountPot > maxPotions) {
                    e.setCancelled(true); // Cancel pickup if max count reached or picking up would exceed max
                    int excessAmount = Math.max(amountPot - remainingDoomPotions, 0);
                    if (excessAmount > 0) {
                        ItemStack excessItem = pickedUpItem.clone();
                        excessItem.setAmount(excessAmount);
                        p.getWorld().dropItem(p.getLocation(), excessItem); // Drop the excess items to the ground
                    }
                    pickedUpItem.setAmount(Math.min(amountPot, remainingDoomPotions)); // Adjust the amount to pick up
                }
                break;

            case "Healing Artifact":
                int currentHealingArtifactCount = NameCheck.extractInvNameCount(p).getOrDefault("Healing Artifact", 0);
                int maxArtifacts = ItemManager.getConfigInt("maxArtifacts");
                int remainingHealingArtifacts = maxArtifacts - currentHealingArtifactCount;
                int amountHeal = pickedUpItem.getAmount();

                if (maxArtifacts >= -1){ break; }

                if (currentHealingArtifactCount == maxArtifacts) {
                    e.setCancelled(true); // Cancel pickup if max count reached
                    break;
                }

                if (currentHealingArtifactCount >= maxArtifacts || currentHealingArtifactCount + amountHeal > maxArtifacts) {
                    e.setCancelled(true); // Cancel pickup if max count reached or picking up would exceed max
                    int excessAmount = Math.max(amountHeal - remainingHealingArtifacts, 0);
                    if (excessAmount > 0) {
                        ItemStack excessItem = pickedUpItem.clone();
                        excessItem.setAmount(excessAmount);
                        p.getWorld().dropItem(p.getLocation(), excessItem); // Drop the excess items to the ground
                    }
                    pickedUpItem.setAmount(Math.min(amountHeal, remainingHealingArtifacts)); // Adjust the amount to pick up
                }
                break;



        }

        // convert item name to config item name format
        String fItem = NameCheck.extractItemName(pickedUpItem).toUpperCase().replace(" ", "_");

        // check if the item is in the config
        for (String item : ItemManager.getConfigItems()) {
            if (item.equalsIgnoreCase(fItem)) {
                ItemManager.setItemOwner(fItem, p);
            }
        }

    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();

        if (!NameCheck.checkName(item)) { return; }
        Passive.removePassiveEffect(p, NameCheck.extractItemName(item));
    }
}
