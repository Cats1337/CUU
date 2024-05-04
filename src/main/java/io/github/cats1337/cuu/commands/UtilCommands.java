package io.github.cats1337.cuu.commands;

import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.command.TabCompleteContext;
import com.marcusslover.plus.lib.text.Text;
import com.marcusslover.plus.lib.text.Tiny;
import io.github.cats1337.cuu.events.DragonSword;
import io.github.cats1337.cuu.utils.ItemManager;
import io.github.cats1337.cuu.utils.ITabCompleterHelper;
import io.github.cats1337.cuu.utils.NameCheck;
import io.github.cats1337.cuu.utils.Rituals;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command(name = "CUU")
public class UtilCommands implements ICommand {
    private final List<String> subCommands = List.of("help", "set", "check", "list", "remove");

    String help = "\n &9&lCats &5&lUberItems &b&lUtils &e&l" + Tiny.of("commands\n" ) +
            " &b/cuu help &7- &fDisplay this message\n" +
            " &b/cuu set <exists/crafted/owner> <itemName> <value> &7- &fSet item information\n" +
            " &b/cuu check <player> &7- &fCheck if a player owns any items\n" +
            " &b/cuu check <exists/crafted/owner> <itemName> &7- &fCheck item information\n" +
            " &b/cuu list &7- &fList all items and their owners\n" +
            " &b/cuu list <player> &7- &fList all items owned by a player\n" +
            " &b/cuu remove owner <itemName> &7- &fRemove the owner of an item\n";


    @Override
    public boolean execute(@NotNull CommandContext cmd){
        CommandSender sender = cmd.sender();
        String[] args = cmd.args();

        // if /cuu has no arguments, send help message

        // if no arguments are provided, send help message
        if (args.length == 0) {
            Text.of(help).send(sender);
            return false;
        }

        String arg = args[0];
        // enable/disable sending log messages to the console
        String prefix = "§8§l[§9§lC§5§lU§b§lU§8§l] §3";
        switch (arg) {
            case "help":
                Text.of(help).send(sender);
                break;

            case "set":
                if (args.length < 4) {
                    Text.of("§cUsage: /CUU set <exists/crafted/owner> <itemName> <value>").send(sender);
                    return false;
                } else {
                    String subCommand = args[1];
                    String itemName = args[2];
                    String confName = NameCheck.convertToConfigName(itemName);
                    String dispName = NameCheck.convertToDisplayName(itemName);
                    String value = args[3];
                    boolean boolValue;
                    switch (value.toLowerCase()) {
                        case "true":
                            boolValue = true;
                            break;
                        case "false":
                            boolValue = false;
                            break;
                        default:
                            Text.of("§cInvalid value, please use 'true' or 'false'.").send(sender);
                            return false;
                    }
                    switch (subCommand) {
                        case "exists":
                            // exists item t/f
                            Text.of(prefix + "§4" + dispName + " §3Crafted status set to: " + (boolValue ? "§aTrue" : "§cFalse")).send(sender);
                            ItemManager.setExists(confName, boolValue);
                            break;
                        case "crafted":
                            ItemManager.setCrafted(confName, boolValue);
                            Text.of(prefix + "§4" + dispName + " §3Crafted status set to: " + (boolValue ? "§aTrue" : "§cFalse")).send(sender);
                            break;
                        default:
                            Text.of("§cInvalid argument. Usage: /CUU set <exists/crafted/owner> <itemName> <value>").send(sender);
                            return false;
                    }
                }
                break;

            case "remove":
                if (args.length < 3) {
                    // /cuu remove bossbar
                    if (args[1].equalsIgnoreCase("bossbar")) {
                        DragonSword.removeBossbars();
                        Rituals.removeBossbars();
                        Text.of(prefix + "§3Bossbars §cremoved§3.").send(sender);
                        return true;
                    }
                    Text.of("§cUsage: /CUU remove <owner> <itemName>").send(sender);
                    return false;
                } else {
                    String subCommand = args[1];
                    String itemName = args[2];
                    String confName = NameCheck.convertToConfigName(itemName);
                    String dispName = NameCheck.convertToDisplayName(itemName);

                    if (subCommand.equals("owner")) {
                        ItemManager.removeItemOwner(confName);
                        Text.of(prefix + "§4" + dispName + " §3Owner §cremoved").send(sender);
                    } else {
                        Text.of("§cInvalid argument. Usage: /CUU remove <owner> <itemName>").send(sender);
                        return false;
                    }
                }
                break;

            case "check":
                if (args.length < 2) {
                    Text.of("§cUsage: /CUU check <player> or /CUU check owner <itemName>").send(sender);
                    return false;
                } else {
                    String subCommand = args[1];
                    if (subCommand.equalsIgnoreCase("exists") || subCommand.equalsIgnoreCase("crafted") || subCommand.equalsIgnoreCase("owner")) {
                        String itemName = args[2];
                        String confName = NameCheck.convertToConfigName(itemName);
                        String dispName = NameCheck.convertToDisplayName(itemName);
                        switch (subCommand) {
                            case "exists":
                                boolean existStatus = ItemManager.getExists(confName);
                                Text.of(prefix + "§4" + dispName + " §3Exists: " + (existStatus ? "§aTrue" : "§cFalse")).send(sender);
                                break;
                            case "crafted":
                                boolean craftStatus = ItemManager.getCrafted(confName);
                                Text.of(prefix + "§4" + dispName + " §3Crafted: " + (craftStatus ? "§aTrue" : "§cFalse")).send(sender);
                                break;
                            case "owner":
                                Text.of(prefix + "§4" + dispName + " §3Owner: §b" + ItemManager.getItemOwner(confName)).send(sender);
                                break;
                        }
                    }
                    else {
                        String playerName = args[1];
                        Player player = sender.getServer().getPlayer(playerName);
                        boolean ownsItems = ItemManager.checkOwnItem(player);
                        if (ownsItems) {
                            Text.of(prefix + "§b" + playerName + " &aowns item(s):").send(sender);
                            String[] items = ItemManager.getOwnedItems(player);
                            for (String item : items) {
                                Text.of("§f - §4" + NameCheck.convertToDisplayName(item)).send(sender);
                            }
                        } else {
                            Text.of(prefix + playerName + " §cdoes not own any items.").send(sender);
                        }
                    }
                }
                break;

            case "list":
                if (args.length > 1) {
                    String target = args[1];
                    if (target.equalsIgnoreCase("owner")) {
                        if (args.length < 3) {
                            Text.of("§cUsage: /CUU list owner <player>").send(sender);
                            return false;
                        }
                        String playerName = args[2];
                        Player player = sender.getServer().getPlayer(playerName);
                        String[] items = ItemManager.getOwnedItems(player);
                        if (items.length == 0) {
                            Text.of(prefix + playerName + " §cdoes not own any items.").send(sender);
                        } else {
                            Text.of(prefix + playerName + "§3 owns: §4" + String.join("§3,§4 ", items)).send(sender);
                        }
                    } else {
                        Text.of("§cInvalid argument. Usage: /CUU list owner <player>").send(sender);
                        return false;
                    }
                } else {
                    String[] items = ItemManager.getConfigItemsString();
                    if (items.length == 0) {
                        Text.of("§3No items found.").send(sender);
                    } else {
                        Text.of("§3Items: §4" + String.join("§3,§4 ", items)).send(sender);
                    }
                }
                break;
        }
        return true;
    }

// "&b/cuu help &7- &fDisplay this message\n" +
//            "&b/cuu get <exists/crafted/owner> <itemName> &7- &fGet item information\n" +
//            "&b/cuu set <exists/crafted/owner> <itemName> <value> &7- &fSet item information\n" +
// "&b/cuu check <player> &7- &fCheck if a player owns any items\n" +
//            "&b/cuu check owner <itemName> &7- &fCheck the owner of an item\n" +
//            "&b/cuu list &7- &fList all items and their owners\n" +
// "&b/cuu list <player> &7- &fList all items owned by a player\n" +
//            "&b/cuu remove owner <itemName> &7- &fRemove the owner of an item";

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull TabCompleteContext tab) {
        @NotNull String[] args = tab.args();

        // list of items from config
        @NotNull String[] items = ItemManager.getConfigItems();

        if (args.length == 1) {
            return ITabCompleterHelper.tabComplete(args[0], subCommands);
        }

        if (args.length == 2) {
            // check <player> | list <player>

            // return list of players AND 'owner' for 'check / list' subcommands
            if (args[0].equalsIgnoreCase("check")) {
                List<String> completions = new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                completions.add("exists");
                completions.add("crafted");
                completions.add("owner");
                return ITabCompleterHelper.tabComplete(args[1], completions);
            }

            if (args[0].equalsIgnoreCase("list")) {
                List<String> completions = new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                completions.add("owner");
                return ITabCompleterHelper.tabComplete(args[1], completions);
            }


            // check owner <itemName>
            if (args[0].equalsIgnoreCase("check") && args[1].equalsIgnoreCase("owner")) {
                // Tab complete item names for 'check' subcommand
                return ITabCompleterHelper.tabComplete(args[1], Arrays.asList(items));
            }

            // check <exists/crafted/owner> ..<itemName> \|/ set <exists/crafted/owner> ..<itemName> <value>
            if (args[0].equalsIgnoreCase("set")) {
                // Tab complete options for 'get' and 'set' subcommands
                return ITabCompleterHelper.tabComplete(args[1], List.of("exists", "crafted", "owner"));
            }

            // remove owner ..<itemName>
            if (args[0].equalsIgnoreCase("remove")) {
                // Tab complete 'owner' for 'remove' subcommand
                return ITabCompleterHelper.tabComplete(args[1], List.of("owner", "bossbar"));
            }
        }

        if (args.length == 3) {

            // check <exists/crafted/owner> <itemName> |/ set <exists/crafted/owner> <itemName> ..<value>
            if (args[0].equalsIgnoreCase("check") || (args[0].equalsIgnoreCase("set") && (args[1].equalsIgnoreCase("owner") || args[1].equalsIgnoreCase("exists") || args[1].equalsIgnoreCase("crafted")))) {
                return ITabCompleterHelper.tabComplete(args[2], Arrays.asList(items));
            }

            // remove owner <itemName>
            if (args[0].equalsIgnoreCase("remove") && args[1].equalsIgnoreCase("owner")) {
                // Tab complete item names for 'remove owner' subcommand
                return ITabCompleterHelper.tabComplete(args[2], Arrays.asList(items));
            }

            // check owner <itemName>
            if (args[0].equalsIgnoreCase("check") && args[1].equalsIgnoreCase("owner")) {
                return ITabCompleterHelper.tabComplete(args[2], Arrays.asList(items));
            }
        }

        // set <exists/crafted/owner> <itemName> <value>
        if (args.length == 4 && (args[0].equalsIgnoreCase("set") && (args[1].equalsIgnoreCase("owner") || args[1].equalsIgnoreCase("exists") || args[1].equalsIgnoreCase("crafted")))) {
            return ITabCompleterHelper.tabComplete(args[3], List.of("true", "false"));
        }
        return new ArrayList<>();
    }
}
