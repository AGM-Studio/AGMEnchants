package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantment;
import me.ashenguard.api.WebReader;
import me.ashenguard.api.numeral.RomanInteger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;


public class EnchantmentManager {
    private static HashMap<String, CustomEnchantment> enchantmentHashMap = new HashMap<>();
    public static CustomEnchantment getCustomEnchantment(String name) { return enchantmentHashMap.getOrDefault(name, null); }
    public static Set<String> getCustomEnchantments() { return enchantmentHashMap.keySet(); }
    public static void save(CustomEnchantment enchantment) {
        if (!enchantment.canRegister()) {
            AGMEnchants.Messenger.Debug("Enchants", "Enchantment ignores to be loaded", "Enchantment= §6" + enchantment.getID());
            return;
        }
        enchantmentHashMap.put(enchantment.getID(), enchantment);
    }


    // ---- Get & Set Enchantments ---- //
    public static HashMap<CustomEnchantment, Integer> extractEnchantments(ItemStack item) {
        HashMap<CustomEnchantment, Integer> enchants = new HashMap<>();
        if (item == null || item.getType().equals(Material.AIR)) return enchants;
        for (CustomEnchantment enchantment: enchantmentHashMap.values()) {
            int level = enchantment.getEnchantLevel(item);
            if (level > 0) enchants.put(enchantment, level);
        }

        return enchants;
    }

    public static void removeAllEnchantments(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return;

        for (CustomEnchantment enchantment : enchantmentHashMap.values())
            removeEnchantment(item, enchantment);
    }
    public static void removeEnchantment(ItemStack item, CustomEnchantment customEnchantment) {
        if (item == null || item.getType().equals(Material.AIR)) return;

        List<String> itemLore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();

        itemLore.removeIf(line -> customEnchantment.getLevelFromLine(line) > 0);

        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(itemLore);
        item.setItemMeta(itemMeta);

        customEnchantment.disenchanted(item);
    }
    public static void addEnchantments(ItemStack item, Map<CustomEnchantment, Integer> customEnchantments) {
        rebase(item, customEnchantments, true);
    }
    public static void addEnchantment(ItemStack item, CustomEnchantment enchantment, int level) {
        rebase(item, Collections.singletonMap(enchantment, level), true);
        enchantment.enchanted(item);
    }

    public static boolean canEnchantItem(CustomEnchantment enchantment, ItemStack item) {
        return enchantment.canEnchantItem(item);
    }
    public static boolean canEnchantItem(Enchantment enchantment, ItemStack item) {
        HashMap<CustomEnchantment, Integer> customEnchants = extractEnchantments(item);
        for (CustomEnchantment customEnchant: customEnchants.keySet())
            if (customEnchant.conflictsWith(enchantment)) return false;
        return enchantment.canEnchantItem(item);
    }

    public static void rebase(ItemStack item) {
        rebase(item, extractEnchantments(item));
    }
    public static void rebase(ItemStack item, Map<CustomEnchantment, Integer> customEnchantments) {
        if (item == null || item.getType().equals(Material.AIR)) return;
        removeAllEnchantments(item);

        List<String> itemLore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
        List<String> lore = new ArrayList<>();

        for (Map.Entry<CustomEnchantment, Integer> customEnchantment: customEnchantments.entrySet())
            lore.add(getColoredName(customEnchantment.getKey(), customEnchantment.getValue()));

        lore.addAll(itemLore);

        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(lore);

        boolean flagged = customEnchantments.size() > 0;
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        for (Map.Entry<Enchantment, Integer> enchantment: enchantments.entrySet()) {
            if (!flagged || !enchantment.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL) || enchantment.getValue() != 0) {
                flagged = false; break;
            }
        }

        if (flagged) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 0, true);
        } else {
            itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            if (itemMeta.hasEnchant(Enchantment.PROTECTION_ENVIRONMENTAL) && itemMeta.getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 0)
                itemMeta.removeEnchant(Enchantment.PROTECTION_ENVIRONMENTAL);
        }

        item.setItemMeta(itemMeta);
    }
    public static void rebase(ItemStack item, Map<CustomEnchantment, Integer> customEnchantments, boolean keep) {
        if (keep) {
            for (Map.Entry<CustomEnchantment, Integer> extracted: extractEnchantments(item).entrySet())
                customEnchantments.put(extracted.getKey(), Math.max(extracted.getValue(), customEnchantments.getOrDefault(extracted.getKey(), 0)));
        }
        rebase(item, customEnchantments);
    }

    public static String getColoredName(CustomEnchantment enchantment) {
        String color = enchantment.isTreasure() ? "§b" : "§7";
        color = enchantment.isCursed() ? "§c" : color;

        return color + enchantment.getName();
    }
    public static String getColoredName(CustomEnchantment enchantment, int level) {
        String name = getColoredName(enchantment);

        if (enchantment.getMaxLevel() == 1) return name;
        return name + " " + RomanInteger.toRoman(Math.min(level, enchantment.getMaxLevel()));
    }

    public static HashMap<String, String> checkEnchantments() {
        HashMap<String, String> found = new HashMap<>();
        boolean enchants = false;
        List<String> lines = new WebReader("https://raw.githubusercontent.com/wiki/Ashengaurd/AGMEnchants/Enchantments.md").readLines();
        for (String line: lines) {
            if (line.startsWith("***")) enchants = true;
            if (enchants && line.startsWith("### ")) {
                String name = line.substring(4).replace("\n", "");
                String version = lines.get(lines.indexOf(line) + 1).replace("Version:", "").replace("\n", "").replace(" ", "");
                found.put(name, version);
            }
        }

        Set<String> installed = getCustomEnchantments();
        List<String> blacklist = AGMEnchants.config.getStringList("Check.BlacklistedEnchantments");
        HashMap<String, String> notInstalled = new HashMap<>();

        for (Map.Entry<String, String> enchant: found.entrySet()) {
            if (blacklist.contains(enchant.getKey())) continue;
            if (!installed.contains(enchant.getKey())) {
                notInstalled.put(enchant.getKey(), enchant.getValue());
                AGMEnchants.Messenger.Debug("Enchants", "A new enchantment found on page", "Name= §6" + enchant.getKey(), "Version= §6" + enchant.getValue());
            } else {
                String version = getCustomEnchantment(enchant.getKey()).getVersion();
                if (!version.equals(enchant.getValue())) {
                    found.put(enchant.getKey(), enchant.getValue());
                    AGMEnchants.Messenger.Debug("Enchants", "An update was found on page for an enchantment", "Name= §6" + enchant.getKey(), "Version= §6" + enchant.getValue(), "Installed Version= §6" + version);
                }
            }
        }

        return notInstalled;
    }
}
