package io.github.cats1337.cuu.items;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import thirtyvirus.uber.UberItem;
import thirtyvirus.uber.helpers.UberAbility;
import thirtyvirus.uber.helpers.UberCraftingRecipe;
import thirtyvirus.uber.helpers.UberRarity;

import java.util.List;

public class doom_leggings extends UberItem {
//    Doom Leggings
//    Netherite Leggings - Curse Of Binding, Protection 5, Blast Protection 5, Unbreakable, Mending and Swift Sneak 3
//    While wearing grants the player fire resistance 1
//    Only one of these can be crafted between all of the players (Command to allow it to be crafted again incase it is destroyed)
//    Once crafted takes 30 minutes to drop from the location it was crafted (Text with Countdown and location displayed as a Boss Health Bar)
//    Once picked up is equiped immediately swapping with current armor player is wearing
    public doom_leggings(Material material, String name, UberRarity rarity, boolean stackable, boolean oneTimeUse, boolean hasActiveEffect, List<UberAbility> abilities, UberCraftingRecipe craftingRecipe) {
        super(material, name, rarity, stackable, oneTimeUse, hasActiveEffect, abilities, craftingRecipe);
    }
    public void onItemStackCreate(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5);
        item.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 5);
        item.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK, 3);
        item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);

        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        ArmorMeta armorMeta = (ArmorMeta) meta;
        ArmorTrim armorTrim = new ArmorTrim(TrimMaterial.REDSTONE, TrimPattern.SILENCE);

        armorMeta.setTrim(armorTrim);
        armorMeta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);

        meta.setUnbreakable(true);
        item.setItemMeta(meta);
    }
    public void getSpecificLorePrefix(List<String> lore, ItemStack item) {
        lore.add("§7Protection V");
        lore.add("§7Blast Protection V");
        lore.add("§7Swift Sneak III");
        lore.add("§cCurse Of Binding");
    }
    public void getSpecificLoreSuffix(List<String> lore, ItemStack item) { }
    public boolean leftClickAirAction(Player player, ItemStack item) { return false; }
    public boolean leftClickBlockAction(Player player, PlayerInteractEvent event, Block block, ItemStack item) { return false; }
    public boolean rightClickAirAction(Player player, ItemStack item) { return false; }
    public boolean rightClickBlockAction(Player player, PlayerInteractEvent event, Block block, ItemStack item) { return false; }
    public boolean shiftLeftClickAirAction(Player player, ItemStack item) { return false; }
    public boolean shiftLeftClickBlockAction(Player player, PlayerInteractEvent event, Block block, ItemStack item) { return false; }
    public boolean shiftRightClickAirAction(Player player, ItemStack item) { return false; }
    public boolean shiftRightClickBlockAction(Player player, PlayerInteractEvent event, Block block, ItemStack item) { return false; }

    public boolean middleClickAction(Player player, ItemStack item) { return false; }
    public boolean hitEntityAction(Player player, EntityDamageByEntityEvent event, Entity target, ItemStack item) { return false; }
    public boolean breakBlockAction(Player player, BlockBreakEvent event, Block block, ItemStack item) { return false; }
    public boolean clickedInInventoryAction(Player player, InventoryClickEvent event, ItemStack item, ItemStack addition) {
        return false; }
    public boolean activeEffect(Player player, ItemStack item) { return false;}
}
