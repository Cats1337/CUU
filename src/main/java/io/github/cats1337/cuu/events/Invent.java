package io.github.cats1337.cuu.events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.cuu.utils.NameCheck;
import io.github.cats1337.cuu.utils.Passive;
import io.github.cats1337.cuu.utils.ItemManager;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class Invent implements Listener {

//   when removing item from inventory, check if it's an UberItem, if it is, remove passive effect
//   drop the item on the ground, move to chest, picks up out of inventory, etc. if it's not in the inventory, remove passive effect

    @EventHandler
    private void inventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        // Check and handle excess items
        handleExcessItems(p, "Healing Artifact", 16);
        handleExcessItems(p, "Doom Potion", 2);

        // Apply or remove passive effects
        if (ItemManager.checkOwnItem(p)) {
            String[] ownedItems = ItemManager.getOwnedItems(p);
            if (ownedItems.length > 0) {
                applyPassiveEffects(p, ownedItems);
            }
        }
    }


    private void handleExcessItems(Player p, String itemName, int maxAllowed) {
        int currentCount = NameCheck.extractInvNameCount(p).getOrDefault(itemName, 0);
        if (currentCount > maxAllowed) {
            removeExcessItems(p, itemName, currentCount - maxAllowed);
        }
    }

    private void applyPassiveEffects(Player p, String[] ownedItems) {
        String[] inventoryItemNames = Arrays.stream(p.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(NameCheck::checkName)
                .map(NameCheck::extractItemName)
                .toArray(String[]::new);

        for (String itemName : ownedItems) {
            if (Arrays.asList(inventoryItemNames).contains(itemName)) {
                Passive.givePassiveEffect(p, itemName);
            } else {
                Passive.removePassiveEffect(p, itemName);
            }
        }
    }

    private void removeExcessItems(Player p, String itemName, int excessAmount) {
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length && excessAmount > 0; i++) {
            ItemStack item = contents[i];
            if (item == null) continue;

            if (NameCheck.extractItemName(item).equals(itemName)) {
                int itemAmount = item.getAmount();
                if (itemAmount <= excessAmount) {
                    excessAmount -= itemAmount;
                    p.getInventory().setItem(i, null);
                    p.getWorld().dropItem(p.getLocation(), item);
                } else {
                    item.setAmount(itemAmount - excessAmount);
                    p.getWorld().dropItem(p.getLocation(), new ItemStack(item.getType(), excessAmount));
                    excessAmount = 0;
                }
            }
        }
    }


    @EventHandler
    private void onInventoryAction(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !NameCheck.checkName(item)) return;

        InventoryAction action = e.getAction();
        if (isActionToUpdatePassiveEffect(action)) {
            String itemName = NameCheck.extractItemName(item);
            if (!p.getInventory().contains(item)) {
                Passive.removePassiveEffect(p, itemName);
                ItemManager.removeItemOwner(itemName);
            } else {
                Passive.givePassiveEffect(p, itemName);
            }
        }
        isPlacingInContainer(e, p);
    }

    private boolean isActionToUpdatePassiveEffect(InventoryAction action) {
        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                action == InventoryAction.PLACE_ALL ||
                action == InventoryAction.PLACE_ONE ||
                action == InventoryAction.PICKUP_HALF ||
                action == InventoryAction.PICKUP_ONE ||
                action == InventoryAction.PICKUP_ALL ||
                action == InventoryAction.HOTBAR_SWAP ||
                action == InventoryAction.HOTBAR_MOVE_AND_READD;
    }

    private void isPlacingInContainer(InventoryClickEvent e, Player p) {

        // Works
        if (e.getClick().isShiftClick()) {
            Inventory clickedInventory = e.getClickedInventory();
            if (clickedInventory == e.getWhoClicked().getInventory()) {
                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem != null && (NameCheck.checkName(clickedItem))) {
                    e.setCancelled(true);
                    p.sendMessage("§cYou cannot shift click Uber Items out of your inventory!");
                }
            }
        }

        // Not working
        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory != e.getWhoClicked().getInventory()){
            ItemStack onCursor = e.getCursor();
            if (onCursor != null && (NameCheck.checkName(onCursor))) {
                e.setCancelled(true);
                p.sendMessage("§cYou cannot place Uber Items into containers!");
            }
        }
    }

    // Detect when player opens a container, they'll have more slots than the default 36 (9x4)
    // if it's just their inventory (36 + 5 armor slots), that's fine
    // don't allow them to place items in chests, hoppers, etc.



    @EventHandler
    private void onPickups(PlayerAttemptPickupItemEvent e) {
        Player p = e.getPlayer();
        ItemStack pickedUpItem = e.getItem().getItemStack();

        if (!NameCheck.checkName(pickedUpItem)) return;

        String itemName = NameCheck.extractItemName(pickedUpItem);
        handlePickedUpDoomItems(e, p, pickedUpItem, itemName);

        // Register ownership for UberItems
        registerItemOwnership(p, pickedUpItem);
    }

    private void handlePickedUpDoomItems(PlayerAttemptPickupItemEvent e, Player p, ItemStack pickedUpItem, String itemName) {
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
                p.getInventory().setHelmet(pickedUpItem);
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

            // Give passive effect for items
            case "Doom Sword": case "Doom Pickaxe": case "Doom Axe":
                Passive.givePassiveEffect(p, itemName);
                break;

            case "Doom Potion": case "Healing Artifact":
                int maxCount = itemName.equals("Doom Potion") ? ItemManager.getConfigInt("maxPotions") : ItemManager.getConfigInt("maxArtifacts");
                if (maxCount < 0) return; // Skip if no limit

                int currentCount = NameCheck.extractInvNameCount(p).getOrDefault(itemName, 0);
                if (currentCount + pickedUpItem.getAmount() > maxCount) {
                    int excess = currentCount + pickedUpItem.getAmount() - maxCount;
                    pickedUpItem.setAmount(maxCount - currentCount);
                    e.getItem().setItemStack(new ItemStack(pickedUpItem.getType(), excess));
                }
        }
    }

    private void registerItemOwnership(Player p, ItemStack item) {
        ItemManager.setItemOwner(NameCheck.extractItemName(item), p);
        Passive.givePassiveEffect(p, NameCheck.extractItemName(item));
    }


    @EventHandler
    private void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack droppedItem = e.getItemDrop().getItemStack();

        if (!NameCheck.checkName(droppedItem)) return;

        String itemName = NameCheck.extractItemName(droppedItem);
        Passive.removePassiveEffect(p, itemName);
//
//        // check if item is in a hopper, if it is, delete it and create a new item with the same properties
//        if (e.getItemDrop().getLocation().getBlock().getType().name().contains("HOPPER")) {
//            e.setCancelled(true);
////            p.getInventory().addItem(droppedItem);
//            p.sendMessage("§cYou cannot drop this item into a hopper!");
//        }


    }
}
