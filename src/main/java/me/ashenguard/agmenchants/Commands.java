package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.api.Messenger;
import me.ashenguard.agmenchants.api.gui.inventories.EnchantsGUI;
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
        if (!(sender instanceof Player)) {
            Messenger.PlayerSend(sender, "This command can be executed by a player");
            return true;
        }

        Player player = (Player) sender;

        EnchantsGUI enchantsGUI = new EnchantsGUI(player);
        enchantsGUI.show();

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
