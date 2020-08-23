package me.ashenguard.agmenchants.commands;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import me.ashenguard.agmenchants.enchants.EnchantsGUI;
import me.ashenguard.api.Commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandCustomEnchantment extends Commands {

    public CommandCustomEnchantment() {
        super(AGMEnchants.getInstance(), "CustomEnchantments", true);
    }

    @Override
    protected boolean playerOnlyCondition(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 0;
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Player player = (Player) sender;

            EnchantsGUI enchantsGUI = new EnchantsGUI(player);
            enchantsGUI.show();
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            String loaded = "§a" + String.join("§r, §a", EnchantmentManager.getCustomEnchantments()) + "§r";
            AGMEnchants.Messenger.send(sender, "Custom enchantments: " + loaded);
        }
    }

    @Override
    public List<String> tabs(CommandSender commandSender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
