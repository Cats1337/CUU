package io.github.cats1337.cuu.events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.cuu.utils.NameCheck;
import io.github.cats1337.cuu.utils.Rituals;
import io.github.cats1337.cuu.utils.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import thirtyvirus.uber.UberItems;
import thirtyvirus.uber.helpers.MenuUtils;
import thirtyvirus.uber.helpers.Utilities;

public class UberCraft implements Listener {
    @EventHandler
    private void onCraftConfigItem(InventoryClickEvent e) {
        if (!NameCheck.isUberItem(e.getCurrentItem())) return;
        if (!e.getView().getTitle().equals("UberItems - Craft Item")) return;
        if (e.getRawSlot() != 23) return; // make sure this only happens when CRAFTING the item, not just clicking one

        ItemStack craftedItem = e.getInventory().getItem(23);
        if (craftedItem == null) return;

        Player p = (Player)e.getWhoClicked(); // get the player
        String confName = NameCheck.convertToConfigNameItem(craftedItem);

        // check if item is a config item
        if (ItemManager.checkItemInConfig(confName)) {
            // check if ritualActive == true
            if(ItemManager.getRitualActive()) {
                Text.of("§4§lA ritual is already active!").send(p);
                p.playSound(p.getLocation(), "entity.bat.death", 1, 0.5F);

                ItemManager.refundMats(confName, p);
                Utilities.scheduleTask(()-> p.setItemOnCursor (null), 1);
                if (p.getInventory().contains(craftedItem) && !ItemManager.getItemOwner(confName).equals(p.getName())) {
                    p.getInventory().removeItem(craftedItem);
                }

                return;
            }
            // check if item exists, if not, check if it has been crafted
            if (!ItemManager.getExists(confName)) {
                // craft the item since it has not been crafted
                if (!ItemManager.getCrafted(confName)) {
                    Utilities.scheduleTask(()-> p.setItemOnCursor (null), 1);
                    p.getInventory().removeItem(craftedItem);
                    handleMenuInteractions(e);
                    return;
                }
            }

            // if the item has already been crafted, or already exists, cancel the event
            if (ItemManager.getCrafted(confName)) {
                Text.of("§cThis item has already been crafted!").send(p);
                p.playSound(p.getLocation(), "entity.bat.death", 1, 0.25F);

                ItemManager.refundMats(confName, p);
                Utilities.scheduleTask(()-> p.setItemOnCursor (null), 1);

                // check if item is in player's inventory
                if (p.getInventory().contains(craftedItem) && !ItemManager.getItemOwner(confName).equals(p.getName())) {
                    p.getInventory().removeItem(craftedItem);
                }
            }
        }
    }

    private void handleMenuInteractions(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getRawSlot() == 23) {
            MenuUtils.pullItem(e);

            // Get the crafted item
            String craftedItem = NameCheck.extractItemName(e.getInventory().getItem(23));
            // get item material
            ItemStack itemMat = e.getInventory().getItem(23);
            String[] configItems = ItemManager.getItems();

            for (String item : configItems) {
                if (craftedItem.equals(item)) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(UberItems.getInstance(), () -> {
                        Utilities.scheduleTask(()-> p.setItemOnCursor (null), 1);
                        assert itemMat != null;
                        p.getInventory().removeItem(itemMat);
                        p.closeInventory();

                        if (p.getLocation().getY() > 170) {
                            Text.of("§cYou must be below Y=170 to perform a ritual!").send(p);
                            return;
                        }

                        Rituals.startRitual(p, item, itemMat);
                        ItemManager.setCrafted(NameCheck.convertToConfigName(item), true);
                    }, 1L);
                }
            }
        }
    }
}
