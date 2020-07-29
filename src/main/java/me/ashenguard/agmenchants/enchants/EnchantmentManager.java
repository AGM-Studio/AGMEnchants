package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.api.Messenger;
import me.ashenguard.agmenchants.api.RomanInteger;
import me.ashenguard.agmenchants.api.WebReader;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;


public class EnchantmentManager {
    private static HashMap<String, CustomEnchantment> enchantmentHashMap = new HashMap<>();
    public static CustomEnchantment getCustomEnchantment(String name) { return enchantmentHashMap.getOrDefault(name, null); }
    public static Set<String> getCustomEnchantments() { return enchantmentHashMap.keySet(); }
    public static void save(CustomEnchantment enchantment) { enchantmentHashMap.put(enchantment.getName(), enchantment); }

    // ---- Randomize ---- //
    public static ItemStack randomBook(CustomEnchantment enchantment) {
        Random random = new Random();
        return enchantment.getBook(random.nextInt(enchantment.getMaxLevel() - 1) + 1);
    }
    public static ItemStack randomBook(boolean force) {
        Random random = new Random();
        if (!force && random.nextBoolean()) return null;

        List<CustomEnchantment> enchantments = (List<CustomEnchantment>) enchantmentHashMap.values();
        CustomEnchantment enchantment = enchantments.get(random.nextInt(enchantments.size()));
        return randomBook(enchantment);
    }
    public static ItemStack randomBook() {
        return randomBook(false);
    }

    // ---- Get & Set Enchantments ---- //
    public static HashMap<CustomEnchantment, Integer> extractEnchantments(ItemStack item) {
        HashMap<CustomEnchantment, Integer> enchants = new HashMap<>();
        if (item == null || item.getType().equals(Material.AIR)) return enchants;
        for (CustomEnchantment enchantment: enchantmentHashMap.values()) {
            int level = getEnchantmentLevel(item, enchantment);
            if (level > 0) enchants.put(enchantment, level);
        }

        return enchants;
    }

    public static int getEnchantmentLevel(ItemStack item, CustomEnchantment enchantment) {
        if (item == null || item.getType().equals(Material.AIR)) return 0;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 0;

        for (String line : lore)
            if (line.contains(enchantment.getName())) {
                if (enchantment.getMaxLevel() == 1) return 1;
                return Math.min(RomanInteger.toInteger(line.substring(line.lastIndexOf(" "))), enchantment.getMaxLevel());
            }

        return 0;
    }

    public static void removeAllEnchantments(ItemStack item) {
        rebase(item, new HashMap<>());
    }
    public static void removeEnchantment(ItemStack item, CustomEnchantment customEnchantment) {
        Map<CustomEnchantment, Integer> customEnchantments = extractEnchantments(item);
        customEnchantments.remove(customEnchantment);
        rebase(item, customEnchantments);
    }
    public static void addEnchantments(ItemStack item, Map<CustomEnchantment, Integer> customEnchantments) {
        rebase(item, customEnchantments, true);
    }
    public static void addEnchantment(ItemStack item, CustomEnchantment enchantment, int level) {
        rebase(item, Collections.singletonMap(enchantment, level), true);
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
        return name + " " + RomanInteger.toRoman(Math.min(level, enchantment.maxLevel));
    }

    public static HashMap<String, String> checkEnchantments() {
        HashMap<String, String> found = new HashMap<>();
        boolean enchants = false;
        List<String> lines = WebReader.readLines("https://raw.githubusercontent.com/wiki/Ashengaurd/AGMEnchants/Enchantments.md");
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
                Messenger.Debug("Enchants", "A new enchantment found on page", "Name= §6" + enchant.getKey(), "Version= §6" + enchant.getValue());
            } else {
                String version = getCustomEnchantment(enchant.getKey()).getVersion();
                if (!version.equals(enchant.getValue())) {
                    found.put(enchant.getKey(), enchant.getValue());
                    Messenger.Debug("Enchants", "An update was found on page for an enchantment", "Name= §6" + enchant.getKey(), "Version= §6" + enchant.getValue(), "Installed Version= §6" + version);
                }
            }
        }

        return notInstalled;
    }

    // ---- Multipliers ---- //
    private static File configFile = new File(AGMEnchants.getPluginFolder(), "VanillaMultipliers.yml");
    static {
        if (!configFile.exists())
            AGMEnchants.getInstance().saveResource("VanillaMultipliers.yml", false);
    }
    private static YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    public static int getMultiplier(Enchantment enchantment, boolean book) {
        return config.getInt(enchantment.getName() + (book ? ".Book" : ".Item"), 1);
    }
    public static int getMultiplier(CustomEnchantment enchantment, boolean book) {
        return book ? enchantment.getBookMultiplier() : enchantment.getItemMultiplier();
    }
}
