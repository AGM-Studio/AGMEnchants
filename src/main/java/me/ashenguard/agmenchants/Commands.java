package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.api.Messenger;
import me.ashenguard.agmenchants.api.gui.inventories.EnchantsGUI;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {
    public Commands() {
        AGMEnchants.getInstance().getCommand("CustomEnchantments").setExecutor(this);
        AGMEnchants.getInstance().getCommand("CustomEnchantments").setTabCompleter(this);

        Messenger.Debug("General", "Commands has been registered");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("CustomEnchantments")) {
            if (!(sender instanceof Player)) {
                if (args.length == 0 || !args[0].equalsIgnoreCase("list")) {
                    Messenger.PlayerSend(sender, "This command can be executed by a player");
                    Messenger.PlayerSend(sender, "Use §6/" + command.getName() + " list§r to see all loaded enchantments");
                } else {
                    String loaded = "§a" + String.join("§r, §a", EnchantmentManager.getCustomEnchantments()) + "§r";
                    Messenger.PlayerSend(sender, "Custom enchantments: " + loaded);
                }
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
                String loaded = "§a" + String.join("§r, §a", EnchantmentManager.getCustomEnchantments()) + "§r";
                Messenger.PlayerSend(sender, "Custom enchantments: " + loaded);
                return true;
            }

            Player player = (Player) sender;

            EnchantsGUI enchantsGUI = new EnchantsGUI(player);
            enchantsGUI.show();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
