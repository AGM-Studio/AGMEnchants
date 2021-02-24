package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.gui.EnchantListGUI;
import me.ashenguard.api.AdvancedCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantCommand extends AdvancedCommand {
    private static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();

    public EnchantCommand() {
        super(AGMEnchants.getInstance(), "Enchants", true);
    }

    @Override
    protected boolean playerOnlyCondition(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 0;
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Player player = (Player) sender;

            EnchantListGUI enchantsGUI = new EnchantListGUI(player);
            enchantsGUI.show();
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            String loaded = "§a" + ENCHANT_MANAGER.STORAGE.getAll().stream().map(Enchant::getName).collect(Collectors.joining("§r, §a")) + "§r";
            AGMEnchants.getMessenger().send(sender, "Enchants: " + loaded);
        }

        if ((args.length == 3 || args.length == 4) && args[0].equalsIgnoreCase("give")) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                AGMEnchants.getMessenger().send(sender, "§cPlayer was not found.");
                return;
            }

            Enchant enchant = ENCHANT_MANAGER.STORAGE.get(args[2].toUpperCase());
            if (enchant == null) {
                AGMEnchants.getMessenger().send(sender, "§cEnchant was not found.");
                return;
            }

            int level = enchant.getMaxLevel();
            if (args.length == 4) {
                try {
                    level = Integer.parseInt(args[3]);
                    level = Math.min(enchant.getMaxLevel(), level);
                    level = Math.max(0, level);
                } catch (NumberFormatException ignored) {}
            }

            ItemStack item = enchant.getEnchantedBook(level);
            player.getInventory().addItem(item);
        }
    }

    @Override
    public List<String> tabs(CommandSender commandSender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 0) return tab;
        if (args.length == 1) tab.addAll(Arrays.asList("list", "give"));
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) tab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) tab.addAll(ENCHANT_MANAGER.STORAGE.getAll().stream().map(e -> e.ID).collect(Collectors.toList()));
        String lastWord = args[args.length - 1];
        return tab.stream().filter(name -> StringUtil.startsWithIgnoreCase(name, lastWord)).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
    }
}
