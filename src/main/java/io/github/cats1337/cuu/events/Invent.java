package io.github.cats1337.cuu.events;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.cuu.utils.NameCheck;
import io.github.cats1337.cuu.utils.Passive;
import io.github.cats1337.cuu.utils.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.GameEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
        handleExcessItems(p, "Healing Artifact", ItemManager.getConfigInt("maxArtifacts"));
        handleExcessItems(p, "Doom Potion", ItemManager.getConfigInt("maxPotions"));
        applyPassiveEffects(p);

        // check if item is in player's inventory, if not, remove passive effect
//        for (ItemStack item : p.getInventory().getContents()) {
//            if (!(item != null && NameCheck.convertToConfigNameItem(item).equals(itemName))) {
//
//            }
//        }

        // check if player owns any items, if not, ignore
        // if they do, check if the item is in the player's inventory, if not, remove passive effect
        if(ItemManager.checkOwnItem(p)) { // if player owns any items
            for (String itemName : ItemManager.getOwnedItems(p)) { // for each item the player owns
                if (Arrays.stream(p.getInventory().getContents()) // check if the item is in the player's inventory
                        .filter(Objects::nonNull) // filter out null items
                        .anyMatch(item -> NameCheck.extractItemName(item).equals(itemName))) { // check if the item is in the player's inventory
                    applyPassiveEffects(p);
                } else {
                    Passive.removePassiveEffect(p, itemName); // remove passive effect
                }
            }
        }

        // get the contents of the container
//        e.getInventory().getContents();
        ItemStack[] contents = e.getInventory().getContents();
        // get the contents of the container
        for (ItemStack item : contents) {
            if (item == null) continue;
            if (NameCheck.checkName(item)) {
                if (ItemManager.checkItemInConfig(NameCheck.convertToConfigNameItem(item))) {
                    if(p.getGameMode().name().equals("CREATIVE")){return;}
                    Text.of("§cNice try...").send(p);
                    // remove from container
                    e.getInventory().remove(item);
                    // place back in owner's inventory
                    if (p.getInventory().firstEmpty() == -1) {
                        p.getWorld().dropItem(p.getLocation(), item);
                    } else {
                        Player itemOwner = ItemManager.getItemOwnerPlayer(NameCheck.extractItemName(item));

                        // check if the owner is within 5 blocks of the container
                        // check if owner is online
                        if (itemOwner != null) {
                            if (itemOwner.getLocation().distance(p.getLocation()) <= 5) {
                                if (itemOwner.getInventory().firstEmpty() != -1) {
                                    itemOwner.getInventory().addItem(item);
                                    return;
                                }
                            } else {
                                p.getWorld().dropItem(p.getLocation(), item);
                                Title titler = Title.title(
                                        Component.text("§4" + NameCheck.extractItemName(item) + " dropped!"),
                                        Component.text("§7" + p.getLocation().getBlockX() + ", " + p.getLocation().getBlockY() + ", " + p.getLocation().getBlockZ())
                                );
                                p.showTitle(titler);
                            }
                        }

                    }
                }
            }
        }

    }

    // check if item is placed in a chest by checking if the item is no longer in the player's inventory
    // if it is, remove it from the container, and drop it on the ground
    // GameEvent CONTAINER_OPEN

    // Disallow placing UberItems in anvils
    @EventHandler
    private void onAnvilUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        // if interacted block is not an anvil, ignore
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.ANVIL) return;
        if(e,getClickedBlock().getType() == Material.ANVIL) Text.of("§cAnvil").send(p);

        if (item == null || !NameCheck.checkName(item)) return;

        if (ItemManager.checkItemInConfig(NameCheck.convertToConfigNameItem(item))) {
            if(p.getGameMode().name().equals("CREATIVE")){return;}
            Text.of("§cYou cannot place this item in an anvil!").send(p);
            e.setCancelled(true);
        }
    }


    @EventHandler
    private void onInventoryAction(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !NameCheck.checkName(item)) return;

        applyPassiveEffects(p);

        Inventory clickedInventory = e.getClickedInventory();
        ItemStack clickedItem = e.getCurrentItem();

        if(e.getInventory().getSize() > 0) {
            if (ItemManager.checkItemInConfig(NameCheck.convertToConfigNameItem(clickedItem))) {
                if((e.getInventory().getSize() == 5 && e.getInventory().getType() != InventoryType.HOPPER) || ((clickedInventory.getType() != InventoryType.PLAYER))) {return;} // Ignore if player's inventory
                if(p.getGameMode().name().equals("CREATIVE")){
                    Text.of("§cContainer prevention bypassed. §7(Creative)").send(p);
                    return;
                }

                e.setCancelled(true);
                Text.of("§cYou cannot place this item in a container!").send(p);

            }
        }

        if(ItemManager.checkItemInConfig(NameCheck.convertToConfigNameItem(e.getCursor()))){
            Passive.removePassiveEffect(p, NameCheck.convertToConfigNameItem(e.getCursor()));
        }

        // if item is not in player's inventory, remove passive effect
        if (!Arrays.stream(p.getInventory().getContents())
                .filter(Objects::nonNull)
                .anyMatch(i -> NameCheck.extractItemName(i).equals(NameCheck.extractItemName(item)))) {
            Passive.removePassiveEffect(p, NameCheck.extractItemName(item));
        }



//        applyPassiveEffects(p);
//        handleInventoryRestrictions(e, p);
    }

    // On right click armor swap/equip
    @EventHandler
    private void onArmorEquip(PlayerArmorChangeEvent e) {
        Player p = e.getPlayer();
        applyPassiveEffects(p);
    }

    @EventHandler
    private void onPickups(PlayerAttemptPickupItemEvent e) {
        Player p = e.getPlayer();
        ItemStack pickedUpItem = e.getItem().getItemStack();

        if (!NameCheck.checkName(pickedUpItem)) return;

        String itemName = NameCheck.extractItemName(pickedUpItem);
        handlePickedUpDoomItems(e, p, pickedUpItem, itemName);

        // Register ownership for UberItems
        ItemManager.setItemOwner(NameCheck.extractItemName(pickedUpItem), p);
    }

    // on item drop
    @EventHandler
    private void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack droppedItem = e.getItemDrop().getItemStack();

        if (!NameCheck.checkName(droppedItem) || p.getGameMode().name().equals("CREATIVE")) return;

//        String itemName = NameCheck.extractItemName(droppedItem);
        e.setCancelled(true);
        Text.of("§cYou cannot drop this item!").send(p);
        applyPassiveEffects(p);
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

    private void handleExcessItems(Player p, String itemName, int maxAllowed) {

        // Extract current item count
        int currentCount = NameCheck.extractInvNameCount(p).getOrDefault(itemName, 0);

        // If the current count is greater than the maximum allowed
        if (currentCount > maxAllowed) {
            int excessAmount = currentCount - maxAllowed;

            // Iterate through the player's inventory to remove excess items
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
    }

    private void applyPassiveEffects(Player p) {
        for (String itemName : ItemManager.getOwnedItems(p)) { // for each item the player owns
            if (Arrays.stream(p.getInventory().getContents()) // check if the item is in the player's inventory
                    .filter(Objects::nonNull) // filter out null items
                    .anyMatch(item -> NameCheck.extractItemName(item).equals(itemName))) { // check if the item is in the player's inventory
                Passive.givePassiveEffect(p, itemName); // give passive effect
            } else {
                Passive.removePassiveEffect(p, itemName); // remove passive effect
            }
        }

    }
}
