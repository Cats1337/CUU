package io.github.cats1337.cuu;

import com.marcusslover.plus.lib.command.CommandManager;
import com.marcusslover.plus.lib.container.ContainerManager;
import io.github.cats1337.cuu.commands.UtilCommands;
import io.github.cats1337.cuu.events.*;
import io.github.cats1337.cuu.items.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import thirtyvirus.uber.UberItems;
import thirtyvirus.uber.helpers.AbilityType;
import thirtyvirus.uber.helpers.UberAbility;
import thirtyvirus.uber.helpers.UberCraftingRecipe;
import thirtyvirus.uber.helpers.UberRarity;

import java.util.Arrays;
import java.util.List;

@Getter
public class CUU extends JavaPlugin {
    private ContainerManager containerManager;
    private CommandManager cmdManager;

    public static CUU getInstance() {
        return CUU.getPlugin(CUU.class);
    }

    public void onEnable() {

        // Enforce UberItems dependancy
        if (Bukkit.getPluginManager().getPlugin("UberItems") == null) {
            this.getLogger().severe("UberItems Addons requires UberItems! disabled because UberItems dependency not found");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getServer().getConsoleSender().sendMessage("§9    ╱|、\n§9   (§b` -§9 7   §3" + getDescription().getName() + " §8- §9Cats §5UberItems §bUtilities\n§9   |、˜〵    §1v" + getDescription().getVersion() + " §8- §7Plugin commissioned by linktr.ee/§cxetho__\n§9   じしˍ,)ノ");


        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // register events and UberItems
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Invent(), this);
        pm.registerEvents(new Projectile(), this);
        pm.registerEvents(new Consumption(), this);
        pm.registerEvents(new DragonSword(), this);

        pm.registerEvents(new Damage(), this);
        pm.registerEvents(new UberCraft(), this);

        // register commands
        cmdManager = CommandManager.get(this);
        cmdManager.register(new UtilCommands());

        registerUberItems();

        containerManager = new ContainerManager();
        containerManager.init(this);
    }

    public void onDisable() {
        // posts exit message in chat
        getLogger().info(getDescription().getName() + " V: " + getDescription().getVersion() + " has been disabled");
        // cancel all active tasks, if any
        Bukkit.getScheduler().cancelTasks(this);
    }

    // NEW UBER ITEM CHECKLIST

    // - make a new class file, named with all lowercase lettering and underscores for spaces
    // - copy the UberItemTemplate class contents into the new class, extend UberItem
    // - make a putItem entry, follow the format of previous items and make sure to give a unique id
    // - write the unique item ability code in the appropriate method

    // - add the following line of code just after executing the item's ability:
    //      onItemUse(player, item); // confirm that the item's ability has been successfully used

    // - if the ability needs a cooldown, prefix it's code with a variation of the following line of code:
    //      if (!Utilities.enforceCooldown(getMain(), player, "name", 1, item, true)) return;

    // - if the item needs work done on create (like adding enchantments, adding other data) refer to onItemStackCreate
    // - if the item needs a prefix or suffix in its description,
    //   refer to the getSpecificLorePrefix and getSpecificLoreSuffix functions, then add the following:
    //      lore.add(ChatColor.RESET + "text goes here");

    // - if you need to store & retrieve ints and strings from items, you can use the following functions:
    //      Utilities.storeIntInItem(getMain(), item, 1, "number tag");
    //      if (Utilities.getIntFromItem(getMain(), item, "number tag") == 1) // { blah blah blah }
    //      (the same case for strings, just storeStringInItem and getStringFromItem)

    private void registerUberItems() {
//        Doom Crown
        UberItems.putItem("doom_crown", new doom_crown(Material.NETHERITE_HELMET, "Doom Crown", UberRarity.MYTHIC, false, false, false,
                List.of(
                        new UberAbility("Doom Crown", AbilityType.NONE, "Grants the player 6 extra hearts while wearing")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.SCULK_CATALYST),
                        new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
                        new ItemStack(Material.SCULK_CATALYST),
                        new ItemStack(Material.MUSIC_DISC_OTHERSIDE),
                        new ItemStack(Material.NETHERITE_HELMET),
                        new ItemStack(Material.MUSIC_DISC_OTHERSIDE),
                        new ItemStack(Material.SCULK_CATALYST),
                        new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
                        new ItemStack(Material.SCULK_CATALYST)
                ), false, 1)));

//       Doom Chestplate
        UberItems.putItem("doom_chestplate", new doom_chestplate(Material.NETHERITE_CHESTPLATE, "Doom Chestplate", UberRarity.MYTHIC, false, false, false,
                List.of(
                        new UberAbility("Doom Chestplate", AbilityType.NONE, "Grants the player strength 2 while wearing")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.SHULKER_SHELL),
                        new ItemStack(Material.END_CRYSTAL),
                        new ItemStack(Material.SHULKER_SHELL),
                        new ItemStack(Material.DRAGON_BREATH),
                        new ItemStack(Material.NETHERITE_CHESTPLATE),
                        new ItemStack(Material.DRAGON_BREATH),
                        new ItemStack(Material.SHULKER_SHELL),
                        new ItemStack(Material.END_CRYSTAL),
                        new ItemStack(Material.SHULKER_SHELL)
                ), false, 1)));

//        Doom Leggings
        UberItems.putItem("doom_leggings", new doom_leggings(Material.NETHERITE_LEGGINGS, "Doom Leggings", UberRarity.MYTHIC, false, false, false,
                List.of(
                        new UberAbility("Doom Leggings", AbilityType.NONE, "Grants the player fire resistance 1 while wearing")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.MAGMA_CREAM),
                        new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        new ItemStack(Material.MAGMA_CREAM),
                        new ItemStack(Material.WITHER_SKELETON_SKULL),
                        new ItemStack(Material.NETHERITE_LEGGINGS),
                        new ItemStack(Material.WITHER_SKELETON_SKULL),
                        new ItemStack(Material.MAGMA_CREAM),
                        new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        new ItemStack(Material.MAGMA_CREAM)
                ), false, 1)));

//        Doom Boots
        UberItems.putItem("doom_boots", new doom_boots(Material.NETHERITE_BOOTS, "Doom Boots", UberRarity.MYTHIC, false, false, false,
                List.of(
                        new UberAbility("Doom Boots", AbilityType.NONE, "Grants the player an extra 40% knockback resistance and speed 1 while wearing")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.RABBIT_FOOT),
                        new ItemStack(Material.TOTEM_OF_UNDYING),
                        new ItemStack(Material.RABBIT_FOOT),
                        new ItemStack(Material.TURTLE_HELMET),
                        new ItemStack(Material.NETHERITE_BOOTS),
                        new ItemStack(Material.TURTLE_HELMET),
                        new ItemStack(Material.RABBIT_FOOT),
                        new ItemStack(Material.TOTEM_OF_UNDYING),
                        new ItemStack(Material.RABBIT_FOOT)
                ), false, 1)));

//        Doom Sword
        UberItems.putItem("doom_sword", new doom_sword(Material.NETHERITE_SWORD, "Doom Sword", UberRarity.MYTHIC, false, false, false,
                List.of(
                        new UberAbility("Doom Sword", AbilityType.NONE, "Grants the player resistance 1 while in inventory")), null));

//        Doom Bow
        UberItems.putItem("doom_bow", new doom_bow(Material.BOW, "Doom Bow", UberRarity.MYTHIC, false, false, false,
                List.of(
                        new UberAbility("Doom Bow", AbilityType.NONE, "30 Second Cooldown per shot, gives slowness 4 for 5 seconds after firing")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.ECHO_SHARD),
                        new ItemStack(Material.ENDER_EYE),
                        new ItemStack(Material.ECHO_SHARD),
                        new ItemStack(Material.END_CRYSTAL),
                        new ItemStack(Material.BOW),
                        new ItemStack(Material.END_CRYSTAL),
                        new ItemStack(Material.ECHO_SHARD),
                        new ItemStack(Material.ENDER_EYE),
                        new ItemStack(Material.ECHO_SHARD)
                ), false, 1)));

//        Doom Axe
        UberItems.putItem("doom_axe", new doom_axe(Material.NETHERITE_AXE, "Doom Axe", UberRarity.MYTHIC, false, false, false,
                List.of(
                        new UberAbility("Doom Axe", AbilityType.NONE, "Steals 50% of health from damage dealt")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.WITHER_ROSE),
                        new ItemStack(Material.WITHER_SKELETON_SKULL),
                        new ItemStack(Material.WITHER_ROSE),
                        new ItemStack(Material.WITHER_SKELETON_SKULL),
                        new ItemStack(Material.NETHERITE_AXE),
                        new ItemStack(Material.WITHER_SKELETON_SKULL),
                        new ItemStack(Material.WITHER_ROSE),
                        new ItemStack(Material.WITHER_SKELETON_SKULL),
                        new ItemStack(Material.WITHER_ROSE)
                ), false, 1)));

//        Doom Pickaxe
        UberItems.putItem("doom_pickaxe", new doom_pickaxe(Material.NETHERITE_PICKAXE, "Doom Pickaxe", UberRarity.MYTHIC, false, false, false,
                List.of(
                        new UberAbility("Doom Pickaxe", AbilityType.NONE, "Grants the player haste 2 while in inventory")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.SCULK_CATALYST),
                        new ItemStack(Material.GUNPOWDER, 64),
                        new ItemStack(Material.SCULK_CATALYST),
                        new ItemStack(Material.SUGAR, 64),
                        new ItemStack(Material.NETHERITE_PICKAXE),
                        new ItemStack(Material.SUGAR, 64),
                        new ItemStack(Material.SCULK_CATALYST),
                        new ItemStack(Material.GUNPOWDER, 64),
                        new ItemStack(Material.SCULK_CATALYST)
                ), false, 1)));

//        Doom Potion
        UberItems.putItem("doom_potion", new doom_potion(Material.SPLASH_POTION, "Doom Potion", UberRarity.EPIC, false, false, false,
                List.of(
                        new UberAbility("Doom Potion", AbilityType.NONE, "Fire Resistance for 10 Minutes, Strength 2 for 1 Minute, Speed 1 for 10 Minutes, Regeneration 1 for 30 Seconds")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.MAGMA_CREAM),
                        new ItemStack(Material.NETHERITE_INGOT),
                        new ItemStack(Material.BLAZE_POWDER),
                        new ItemStack(Material.NETHER_WART),
                        new ItemStack(Material.POTION),
                        new ItemStack(Material.NETHER_WART),
                        new ItemStack(Material.BLAZE_POWDER),
                        new ItemStack(Material.NETHERITE_INGOT),
                        new ItemStack(Material.MAGMA_CREAM)
                ), false, 1)));

//        Healing Artifact
        UberItems.putItem("healing_artifact", new healing_artifact(Material.HEART_POTTERY_SHERD, "Healing Artifact", UberRarity.EPIC, true, true, false,
                List.of(
                        new UberAbility("Healing Artifact", AbilityType.RIGHT_CLICK, "Regeneration 2 for 5 Seconds, Instantly heals 3 hearts")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.GLISTERING_MELON_SLICE),
                        new ItemStack(Material.PAPER),
                        new ItemStack(Material.GLISTERING_MELON_SLICE),
                        new ItemStack(Material.PAPER),
                        new ItemStack(Material.GOLDEN_CARROT),
                        new ItemStack(Material.PAPER),
                        new ItemStack(Material.GLISTERING_MELON_SLICE),
                        new ItemStack(Material.PAPER),
                        new ItemStack(Material.GLISTERING_MELON_SLICE)
                ), false, 1)));

//        Bucket Of Golden Carrots
        UberItems.putItem("bucket_of_golden_carrots", new golden_carrot_bucket(Material.MILK_BUCKET, "Bucket Of Golden Carrots", UberRarity.RARE, false, false, false,
                List.of(
                        new UberAbility("Bucket Of Golden Carrots", AbilityType.NONE, "Grants full hunger and saturation")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.GOLDEN_CARROT, 64),
                        new ItemStack(Material.GOLD_BLOCK),
                        new ItemStack(Material.GOLDEN_CARROT, 64),
                        new ItemStack(Material.GOLD_BLOCK),
                        new ItemStack(Material.MILK_BUCKET),
                        new ItemStack(Material.GOLD_BLOCK),
                        new ItemStack(Material.GOLDEN_CARROT, 64),
                        new ItemStack(Material.GOLD_BLOCK),
                        new ItemStack(Material.GOLDEN_CARROT, 64)
                ), false, 1)));

//        Netherite Apple
        UberItems.putItem("netherite_apple", new netherite_apple(Material.GOLDEN_APPLE, "Netherite Apple", UberRarity.EPIC, true, true, false,
                List.of(
                        new UberAbility("Netherite Apple", AbilityType.NONE, "Resistance 1 for 10 Seconds, Regeneration 3 for 5 Seconds, Absorption 3 for 60 Seconds, Fire Resistance for 60 Seconds")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.AIR),
                        new ItemStack(Material.NETHERITE_SCRAP),
                        new ItemStack(Material.AIR),
                        new ItemStack(Material.NETHERITE_SCRAP),
                        new ItemStack(Material.GOLDEN_APPLE),
                        new ItemStack(Material.NETHERITE_SCRAP),
                        new ItemStack(Material.AIR),
                        new ItemStack(Material.NETHERITE_SCRAP),
                        new ItemStack(Material.AIR)
                ), false, 1)));

    }
}