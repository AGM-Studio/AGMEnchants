package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Describable;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.Rune;
import me.ashenguard.agmenchants.gui.CategoryGUI;
import me.ashenguard.agmenchants.gui.PreviewGUI;
import me.ashenguard.api.AdvancedCommand;
import me.ashenguard.exceptions.NullAssertionError;
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

public class CommandManager {
    public static void register() {
        new EnchantCommand();
        new RuneCommand();
    }

    private static class EnchantCommand extends AdvancedCommand {
        public EnchantCommand() {
            super(AGMEnchants.getInstance(), "Enchants");

            playerRequired = (args) -> args.length == 0 || (args.length == 2 && "view".equalsIgnoreCase(args[0]));
        }

        @Override
        public void run(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) new CategoryGUI((Player) sender).show();

            if (args.length == 2 && "view".equalsIgnoreCase(args[0])) {
                Describable enchant = EnchantManager.STORAGE.get(args[1]);
                NullAssertionError.check(AGMEnchants.getTranslations().get("EnchantNotFound"), enchant);
                new PreviewGUI((Player) sender, enchant).show();
            }

            if ((args.length == 3 || args.length == 4) && ("give".equalsIgnoreCase(args[0]) || "enchant".equalsIgnoreCase(args[0]))) {
                Player player = Bukkit.getPlayer(args[1]);
                Enchant enchant = EnchantManager.STORAGE.get(args[2]);

                //noinspection ConstantConditions
                NullAssertionError.check(AGMEnchants.getTranslations().get("PlayerNotFound"), player);
                NullAssertionError.check(AGMEnchants.getTranslations().get("EnchantNotFound"), enchant);

                int level = enchant.getMaxLevel();
                if (args.length == 4) {
                    try {
                        level = Integer.parseInt(args[3]);
                        level = Math.min(enchant.getMaxLevel(), level);
                        level = Math.max(0, level);
                    } catch (NumberFormatException ignored) {
                    }
                }

                if ("give".equalsIgnoreCase(args[0]))
                    player.getInventory().addItem(enchant.getEnchantedBook(level));

                if ("enchant".equalsIgnoreCase(args[0]) && enchant.canEnchantItem(player.getInventory().getItemInMainHand()))
                    enchant.applyEnchant(player.getInventory().getItemInMainHand(), level);
            }
        }

        @Override
        public List<String> tabs(CommandSender commandSender, Command command, String label, String[] args) {
            List<String> tab = new ArrayList<>();
            if (args.length == 0) return tab;
            if (args.length == 1) tab.addAll(Arrays.asList("view", "give", "enchant"));
            if (args.length == 2 && ("give".equalsIgnoreCase(args[0]) || "enchant".equalsIgnoreCase(args[0])))
                tab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
            if (args.length == 3 && ("give".equalsIgnoreCase(args[0]) || "enchant".equalsIgnoreCase(args[0])))
                tab.addAll(EnchantManager.STORAGE.getAll().stream().map(e -> e.getKey().toString()).collect(Collectors.toList()));
            if (args.length == 4 && ("give".equalsIgnoreCase(args[0]) || "enchant".equalsIgnoreCase(args[0]))) {
                Enchant enchant = EnchantManager.STORAGE.get(args[2]);
                if (enchant != null) for (int i = 0; i < enchant.getMaxLevel(); i++)
                    tab.add(String.valueOf(i));
            }
            String lastWord = args[args.length - 1];
            return tab.stream().filter(name -> StringUtil.startsWithIgnoreCase(name, lastWord)).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
        }
    }

    private static class RuneCommand extends AdvancedCommand {
        public RuneCommand() {
            super(AGMEnchants.getInstance(), "Runes");

            playerRequired = (args) -> args.length == 0 || (args.length == 2 && "view".equalsIgnoreCase(args[0]));
        }

        @Override
        public void run(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) new CategoryGUI((Player) sender).show();

            if (args.length == 2 && "view".equalsIgnoreCase(args[0])) {
                Describable rune = RuneManager.STORAGE.get(args[1]);
                NullAssertionError.check(AGMEnchants.getTranslations().get("EnchantNotFound"), rune);
                new PreviewGUI((Player) sender, rune).show();
            }

            if (args.length == 3 && ("give".equalsIgnoreCase(args[0]) || "enchant".equalsIgnoreCase(args[0]))) {
                Player player = Bukkit.getPlayer(args[1]);
                Rune rune = RuneManager.STORAGE.get(args[2]);

                //noinspection ConstantConditions
                NullAssertionError.check(AGMEnchants.getTranslations().get("PlayerNotFound"), player);
                NullAssertionError.check(AGMEnchants.getTranslations().get("RuneNotFound"), rune);

                if ("give".equalsIgnoreCase(args[0]))
                    player.getInventory().addItem(rune.getRune());

                if ("enchant".equalsIgnoreCase(args[0]) && rune.canRuneItem(player.getInventory().getItemInMainHand()))
                    rune.applyRune(player.getInventory().getItemInMainHand());
            }
        }

        @Override
        public List<String> tabs(CommandSender commandSender, Command command, String label, String[] args) {
            List<String> tab = new ArrayList<>();
            if (args.length == 0) return tab;
            if (args.length == 1) tab.addAll(Arrays.asList("view", "give", "apply"));
            if (args.length == 2 && ("give".equalsIgnoreCase(args[0]) || "apply".equalsIgnoreCase(args[0])))
                tab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
            if (args.length == 3 && ("give".equalsIgnoreCase(args[0]) || "apply".equalsIgnoreCase(args[0])))
                tab.addAll(EnchantManager.STORAGE.getAll().stream().map(e -> e.getKey().toString()).collect(Collectors.toList()));
            String lastWord = args[args.length - 1];
            return tab.stream().filter(name -> StringUtil.startsWithIgnoreCase(name, lastWord)).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
        }
    }
}
