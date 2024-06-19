package io.github.cats1337.cuu.items;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import thirtyvirus.uber.UberItem;
import thirtyvirus.uber.helpers.UberAbility;
import thirtyvirus.uber.helpers.UberCraftingRecipe;
import thirtyvirus.uber.helpers.UberRarity;

import java.util.ArrayList;
import java.util.List;


public class doom_shield extends UberItem {
// Shield (Unbreakable)
// Only one of these can be crafted between all of the players
// Once crafted takes 30 minutes to drop from the location it was crafted (Text with Countdown and location displayed as a Boss Health Bar)

// When Shield is broken by an axe there is a 10% chance that the attacker is launched into the sky (30 blocks)
    public doom_shield(Material material, String name, UberRarity rarity, boolean stackable, boolean oneTimeUse, boolean hasActiveEffect, List<UberAbility> abilities, UberCraftingRecipe craftingRecipe) {
        super(material, name, rarity, stackable, oneTimeUse, hasActiveEffect, abilities, craftingRecipe);
    }
    public void onItemStackCreate(ItemStack item) {
        // 10% to launch attacker into the sky (30 blocks) when shield is broken by an axe
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);

        if (meta instanceof BlockStateMeta blockStateMeta) {
            if (blockStateMeta.getBlockState() instanceof Banner banner) {

                List<Pattern> patterns = new ArrayList<>();

                patterns.add(new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_TOP)); // tt
                patterns.add(new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_BOTTOM)); // bt
                patterns.add(new Pattern(DyeColor.BLACK, PatternType.DIAGONAL_LEFT_MIRROR)); // lud
                patterns.add(new Pattern(DyeColor.BLACK, PatternType.DIAGONAL_RIGHT)); // rd
                patterns.add(new Pattern(DyeColor.RED, PatternType.TRIANGLES_BOTTOM)); // bts
                patterns.add(new Pattern(DyeColor.RED, PatternType.TRIANGLES_TOP)); // tts
                patterns.add(new Pattern(DyeColor.RED, PatternType.STRAIGHT_CROSS)); // sc
                patterns.add(new Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER)); // cs
                patterns.add(new Pattern(DyeColor.BLACK, PatternType.RHOMBUS_MIDDLE)); // mr
                patterns.add(new Pattern(DyeColor.BLACK, PatternType.CURLY_BORDER)); // cbo
                patterns.add(new Pattern(DyeColor.RED, PatternType.SKULL)); // sku


                banner.setPatterns(patterns);
                banner.update();
                blockStateMeta.setBlockState(banner);
            }
        }

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // This hides banner patterns, HIDE_ATTRIBUTES doesn't...?
        item.setItemMeta(meta);

    }
    public void getSpecificLorePrefix(List<String> lore, ItemStack item) { }
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
    public boolean clickedInInventoryAction(Player player, InventoryClickEvent event, ItemStack item, ItemStack addition) { return false; }
    public boolean activeEffect(Player player, ItemStack item) {return false; }
}

