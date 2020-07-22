package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.api.RomanInteger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EnchantmentManager {
    private static HashMap<String, CustomEnchantment> enchantmentHashMap = new HashMap<>();
    public static CustomEnchantment getEnchantment(String name) { return enchantmentHashMap.getOrDefault(name, null); }
    public static Set<String> getEnchantments() { return enchantmentHashMap.keySet(); }
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
        if (item == null) return enchants;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return enchants;

        for (String line : lore) {
            CustomEnchantment enchantment = getEnchantment(line.substring(2, line.lastIndexOf(" ")));
            if (enchantment != null) {
                int level = RomanInteger.toInteger(line.substring(line.lastIndexOf(" ")));
                enchants.put(enchantment, level);
            }
        }

        return enchants;
    }

    public static int getEnchantmentLevel(ItemStack item, CustomEnchantment enchantment) {
        if (item == null) return 0;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 0;

        for (String line : lore)
            if (line.contains(enchantment.getName()))
                return RomanInteger.toInteger(line.substring(line.lastIndexOf(" ")));

        return 0;
    }

    public static ItemStack clearEnchantments(ItemStack item) {
        if (item == null) return null;
        ItemMeta itemMeta = item.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore == null) return item;

        lore.removeIf(line -> getEnchantment(line.substring(2, line.lastIndexOf(" "))) != null);

        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        glow(item);

        return item;
    }
    public static ItemStack addEnchantments(ItemStack item, HashMap<CustomEnchantment, Integer> enchantments) {
        if (item == null) return null;

        for (Map.Entry<CustomEnchantment, Integer> enchantment : enchantments.entrySet()) addEnchantment(item, enchantment.getKey(), enchantment.getValue());

        return item;
    }
    public static void addEnchantment(ItemStack item, CustomEnchantment enchantment, int level) {
        ItemMeta itemMeta = item.getItemMeta();

        List<String> lore = itemMeta.getLore();
        if (lore == null) lore = new ArrayList<>();

        lore.add(getColoredName(enchantment) + " " + RomanInteger.toRoman(level + 1));
        itemMeta.setLore(lore);

        item.setItemMeta(itemMeta);

        glow(item);
    }

    public static void glow(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();

        boolean flagged = extractEnchantments(item).entrySet().size() > 0;
        Map<Enchantment, Integer> vanillaEnchants = itemMeta.getEnchants();
        for (Map.Entry<Enchantment, Integer> vanillaEnchant: vanillaEnchants.entrySet()) {
            if (!vanillaEnchant.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL) || vanillaEnchant.getValue() != 0) {
                flagged = false;
                break;
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

    public static String getColoredName(CustomEnchantment enchantment) {
        String color = enchantment.isTreasure() ? "§b" : "§7";
        color = enchantment.isCursed() ? "§c" : color;

        return color + enchantment.getName();
    }
}
