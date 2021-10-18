package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.nbt.NBTItem;
import me.ashenguard.api.nbt.NBTList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@SuppressWarnings({"UnusedReturnValue", "unused"})
public class LoreManager {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();

    private static final String NBT_SECURE_LORE = "SecureLore";
    private static final String NBT_SECURE_DONE = "Secured";
    private static final Configuration config = new Configuration(PLUGIN, "Features/lore.yml");

    private static String SEPARATOR_LINE = "§f------------------------------";
    private static boolean ABOVE_LORE = true;
    private static boolean SHOW_LORE = false;
    private static boolean COMPRESSIBLE = false;
    private static int COMPRESS_LIMIT = 8;
    private static int LINE_LIMIT = 50;
    private static String SPACING = "    ";

    public static void loadConfig() {
        ABOVE_LORE = config.getBoolean("AboveItemLore", ABOVE_LORE);
        SHOW_LORE = config.getBoolean("ShowLore", SHOW_LORE);
        SEPARATOR_LINE = config.getString("SeparatorLine", SEPARATOR_LINE);
        COMPRESSIBLE = config.getBoolean("Compress", COMPRESSIBLE);
        COMPRESS_LIMIT = config.getInt("CompressLimit", COMPRESS_LIMIT);
        LINE_LIMIT = config.getInt("LineLimit", LINE_LIMIT);
        SPACING = config.getString("Spacing", SPACING);

        if (config.getBoolean("UpdateOnItemPickUp", false)) {
            Bukkit.getServer().getPluginManager().registerEvents(new Updater(), PLUGIN);
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public static boolean isLoreSecured(ItemStack item) {
        NBTItem nbt = new NBTItem(item, true);
        return nbt.hasKey(NBT_SECURE_DONE) && nbt.getBoolean(NBT_SECURE_DONE);
    }
    public static List<String> secureLore(ItemStack item) {
        if (!isLoreSecured(item))
            return setSecureLore(item, getItemLore(item));
        return getSecureLore(item);
    }
    public static List<String> setSecureLore(ItemStack item, List<String> lore) {
        if (item == null || item.getType().equals(Material.AIR)) return lore;
        NBTItem nbt = new NBTItem(item, true);

        NBTList<String> list = nbt.getStringList(NBT_SECURE_LORE);
        list.clear();
        list.addAll(lore);

        nbt.setBoolean(NBT_SECURE_DONE, true);
        return lore;
    }
    public static List<String> getSecureLore(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return null;
        NBTItem nbt = new NBTItem(item);
        return nbt.hasKey(NBT_SECURE_DONE) && nbt.getBoolean(NBT_SECURE_DONE) ? new ArrayList<>(nbt.getStringList(NBT_SECURE_LORE)) : new ArrayList<>();
    }

    public static void updateItem(@NotNull ItemStack item) {
        final List<String> lore = new ArrayList<>();

        if (RuneManager.isItemRune(item)) {
            Rune rune = RuneManager.getItemRune(item);
            if (rune != null) lore.add(rune.getLore());
        } else {
            lore.addAll(LoreManager.getRuinsLore(item));
            lore.addAll(LoreManager.getEnchantsLore(item));

            final List<String> secureLore = secureLore(item);
            if (secureLore != null && secureLore.size() > 0) {
                lore.add(ABOVE_LORE ? lore.size() : 0, SEPARATOR_LINE);
                lore.addAll(ABOVE_LORE ? lore.size() : 0, secureLore);
            }
        }

        List<ItemFlag> flags = new ArrayList<>();
        flags.add(ItemFlag.HIDE_ENCHANTS);
        if (item.getType() == Material.ENCHANTED_BOOK) flags.add(ItemFlag.HIDE_POTION_EFFECTS);

        setItemDisplay(item, null, trimList(lore), flags);
    }
    public static ItemStack setItemDisplay(ItemStack item, String name, List<String> lore, Iterable<ItemFlag> flags) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (lore != null) meta.setLore(trimList(lore));
        if (name != null) meta.setDisplayName(name);
        if (flags != null) flags.forEach(meta::addItemFlags);
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> getItemLore(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return new ArrayList<>();

        List<String> lore = itemMeta.getLore();
        if (lore == null) return new ArrayList<>();
        return lore;
    }
    private static List<String> trimList(List<String> list) {
        List<String> filter = new ArrayList<>();
        for (String line: list) if (!filter.contains(line)) filter.add(line);

        List<String> stringList = new ArrayList<>();
        for (String line: filter) stringList.addAll(breakString(line));

        while (!stringList.isEmpty() && (stringList.get(0).isEmpty() || stringList.get(0).equals(SEPARATOR_LINE))) stringList.remove(0);
        while (!stringList.isEmpty() && (stringList.get(stringList.size() - 1).isEmpty()  || stringList.get(stringList.size() - 1).equals(SEPARATOR_LINE))) stringList.remove(stringList.size() - 1);

        return stringList;
    }
    private static List<String> breakString(String string) {
        List<String> list = new ArrayList<>();

        Pattern pattern = Pattern.compile("(§.)+");
        while (true){
            int index = string.indexOf(' ');
            while (index != -1 && ChatColor.stripColor(string.substring(0, index)).length() < LINE_LIMIT)
                index = string.indexOf(' ', index + 1);
            index = index == -1 ? string.length() : index;
            String line = string.substring(0, index);
            list.add(line);
            string = string.substring(line.length());
            if (string.isEmpty()) break;
            String color = "";
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) color = matcher.group();
            string = color + string;
        }
        return list;
    }
    private static List<String> getEnchantsLore(ItemStack item) {
        List<Map.Entry<Enchant, Integer>> list = EnchantManager.extractEnchants(item).entrySet().stream()
                .filter(enchant -> enchant.getValue() > 0).collect(Collectors.toUnmodifiableList());

        HashMap<Enchant, Integer> enchants = new HashMap<>();
        list.forEach(entry -> enchants.put(entry.getKey(), entry.getValue()));

        List<String> lore = new ArrayList<>();
        if (COMPRESSIBLE && enchants.size() > COMPRESS_LIMIT) {
            lore.add(enchants.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getKey().getKey()))
                    .map(enchant -> enchant.getKey().getColoredName(enchant.getValue()))
                    .collect(Collectors.joining("§r, ")));
        } else {
            for (Map.Entry<Enchant, Integer> enchant : enchants.entrySet()) {
                lore.add(enchant.getKey().getColoredName(enchant.getValue()));
                if (SHOW_LORE) lore.add(SPACING + PHManager.translate(enchant.getKey().getLore(enchant.getValue())));
            }
        }
        return lore;
    }
    private static List<String> getRuinsLore(ItemStack item) {
        List<String> lore = new ArrayList<>();
        Rune rune = RuneManager.getItemRune(item);
        if (rune != null) {
            lore.add(rune.getColoredName());
            if (SHOW_LORE) lore.add(SPACING + PHManager.translate(rune.getLore()));
        }

        return lore;
    }

    private static class Updater implements Listener {
        @EventHandler
        public void onItemPickup(EntityPickupItemEvent event) {
            LoreManager.updateItem(event.getItem().getItemStack());
        }
    }
}
