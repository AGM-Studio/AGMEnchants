package me.ashenguard.agmenchants.runes;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.gui.RuneGUI;
import me.ashenguard.api.AdvancedCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RuneCommand extends AdvancedCommand {
    private static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();

    public RuneCommand() {
        super(AGMEnchants.getInstance(), "Runes", true);
    }

    @Override
    protected boolean playerOnlyCondition(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 0;
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Player player = (Player) sender;

            RuneGUI runesGUI = new RuneGUI(player);
            runesGUI.show();
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            String loaded = "§a" + RUNE_MANAGER.STORAGE.getAll().stream().map(Rune::getName).collect(Collectors.joining("§r, §a")) + "§r";
            AGMEnchants.getMessenger().send(sender, "Runes: " + loaded);
        }

        if ((args.length == 3) && args[0].equalsIgnoreCase("give")) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                AGMEnchants.getMessenger().send(sender, "§cPlayer was not found.");
                return;
            }
            
            Rune rune = RUNE_MANAGER.STORAGE.get(args[2].toUpperCase());
            if (rune == null) {
                AGMEnchants.getMessenger().send(sender, "§cRune was not found.");
                return;
            }

            player.getInventory().addItem(rune.getRune());
        }
    }

    @Override
    public List<String> tabs(CommandSender commandSender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 0) return tab;
        if (args.length == 1) tab.addAll(Arrays.asList("list", "give"));
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) tab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) tab.addAll(RUNE_MANAGER.STORAGE.getAll().stream().map(e -> e.ID).collect(Collectors.toList()));
        String lastWord = args[args.length - 1];
        return tab.stream().filter(name -> StringUtil.startsWithIgnoreCase(name, lastWord)).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
    }
}
