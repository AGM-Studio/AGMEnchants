package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;


public class EnchantmentManager {
    private static HashMap<String, CustomEnchantment> enchantmentHashMap = new HashMap<>();
    public static CustomEnchantment getCustomEnchantment(String id) { return enchantmentHashMap.getOrDefault(id, null); }
    public static Set<String> getCustomEnchantments() { return enchantmentHashMap.keySet(); }
    public static void save(CustomEnchantment enchantment) {
        if (!enchantment.canRegister()) {
            AGMEnchants.Messenger.Warning("An enchantment ignores to be loaded", "Enchantment= §6" + enchantment.getID());
            return;
        }
        enchantmentHashMap.put(enchantment.getID(), enchantment);
    }

    // ---- Get & Set Enchantments ---- //
    public static HashMap<CustomEnchantment, Integer> extractEnchantments(ItemStack item) {
        HashMap<CustomEnchantment, Integer> enchants = new HashMap<>();
        if (item == null || item.getType().equals(Material.AIR)) return enchants;
        for (CustomEnchantment enchantment: enchantmentHashMap.values()) {
            int level = enchantment.getEnchantmentLevel(item);
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
    public static void addEnchantments(ItemStack item, Map<CustomEnchantment, Integer> customEnchantments, boolean call) {
        for (Map.Entry<CustomEnchantment, Integer> enchantment: customEnchantments.entrySet())
            addEnchantment(item, enchantment.getKey(), enchantment.getValue(), call);
    }
    public static void addEnchantments(ItemStack item, Map<CustomEnchantment, Integer> customEnchantments) {
        addEnchantments(item, customEnchantments, false);
    }
    public static void addEnchantment(ItemStack item, CustomEnchantment enchantment, int level, boolean call) {
        rebase(item, Collections.singletonMap(enchantment, level), true);
        if (call) enchantment.enchanted(item, level);
    }
    public static void addEnchantment(ItemStack item, CustomEnchantment enchantment, int level) {
        addEnchantment(item, enchantment, level, false);
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

        List<CustomEnchantment> enchants = new ArrayList<>(customEnchantments.keySet());
        Collections.sort(enchants);
        for (CustomEnchantment enchant: enchants)
            lore.add(enchant.getColoredName(customEnchantments.get(enchant)));

        lore.addAll(itemLore);

        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(lore);

        boolean flagged = customEnchantments.size() > 0;
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        for (Map.Entry<Enchantment, Integer> enchantment: enchantments.entrySet()) {
            if (!flagged || !enchantment.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL) || enchantment.getValue() != 0)
                flagged = false;
            if (enchantment.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL) && enchantment.getValue() == 0)
                item.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
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
        Map<CustomEnchantment, Integer> previousCustomEnchantments = keep ? extractEnchantments(item) : new HashMap<>();
        for (Map.Entry<CustomEnchantment, Integer> customEnchantment: customEnchantments.entrySet())
            previousCustomEnchantments.put(customEnchantment.getKey(), Math.max(customEnchantment.getValue(), previousCustomEnchantments.getOrDefault(customEnchantment.getKey(), 0)));

        rebase(item, previousCustomEnchantments);
    }
}
