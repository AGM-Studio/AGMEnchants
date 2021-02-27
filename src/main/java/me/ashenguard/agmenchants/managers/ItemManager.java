package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.EnchantManager;
import me.ashenguard.agmenchants.enchants.VanillaEnchant;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.agmenchants.runes.RuneManager;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.nbt.NBTItem;
import me.ashenguard.api.nbt.NBTList;
import me.ashenguard.api.utils.extra.Pair;
import me.ashenguard.api.utils.extra.RandomCollection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@SuppressWarnings("UnusedReturnValue")
public class ItemManager {
    private static EnchantManager ENCHANT_MANAGER = null;
    private static RuneManager RUNE_MANAGER = null;

    private static final String NBT_SECURE_LORE = "SecureLore";

    private String SEPARATOR_LINE = "§f------------------------------";
    private boolean ABOVE_LORE = true;
    private boolean SHOW_LORE = false;
    private boolean COMPRESSIBLE = false;
    private int COMPRESS_LIMIT = 8;
    private int LINE_LIMIT = 50;

    public void loadConfigs() {
        FileConfiguration config = AGMEnchants.getConfiguration();

        ABOVE_LORE = config.getBoolean("LoreConfig.AboveItemLore", true);
        SHOW_LORE = config.getBoolean("LoreConfig.ShowLore", false);
        SEPARATOR_LINE = config.getString("LoreConfig.SeparatorLine", "§f------------------------------");
        COMPRESSIBLE = config.getBoolean("LoreConfig.Compress", false);
        COMPRESS_LIMIT = config.getInt("LoreConfig.CompressLimit", 8);
        LINE_LIMIT = config.getInt("LoreConfig.LineLimit", 50);

        ENCHANT_MANAGER = AGMEnchants.getEnchantManager();
        RUNE_MANAGER = AGMEnchants.getRuneManager();
    }

    public void secureItemLore(ItemStack item) {
        if (ENCHANT_MANAGER.extractEnchants(item, false).size() > 0) return;
        setSecureItemLore(item, getItemLore(item));
    }
    public void setSecureItemLore(ItemStack item, List<String> lore) {
        if (item == null || item.getType().equals(Material.AIR)) return;
        NBTItem nbt = new NBTItem(item, true);
        NBTList<String> list = nbt.getStringList(NBT_SECURE_LORE);
        list.clear();
        list.addAll(lore);
    }
    public List<String> getSecureLore(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return null;
        NBTItem nbt = new NBTItem(item);
        return nbt.hasKey(NBT_SECURE_LORE) ? new ArrayList<>(nbt.getStringList(NBT_SECURE_LORE)) : null;
    }
    public List<String> getItemLore(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta() == null) return new ArrayList<>();
        List<String> secure = getSecureLore(item);
        if (secure == null || secure.size() == 0) {
            if (ENCHANT_MANAGER.extractEnchants(item, false).size() > 0) return new ArrayList<>();
            ItemMeta meta = item.getItemMeta();
            return meta.hasLore() ? meta.getLore() : new ArrayList<>();
        } else return secure;
    }
    public ItemStack setItemDisplay(ItemStack item, String name, List<String> lore, Iterator<ItemFlag> flags) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (lore != null) meta.setLore(trimList(lore));
        if (name != null) meta.setDisplayName(name);
        if (flags != null) flags.forEachRemaining(meta::addItemFlags);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack applyItemLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;
        if (ENCHANT_MANAGER.extractEnchants(item).size() == 0) return item;

        if (item.getEnchantments().size() == 0) item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        else if (item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 0) item.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
        
        final List<ItemFlag> flags = new ArrayList<>();
        flags.add(ItemFlag.HIDE_ENCHANTS);
        if (item.getType().equals(Material.ENCHANTED_BOOK)) flags.add(ItemFlag.HIDE_POTION_EFFECTS);
        final List<String> oldLore = getItemLore(item);
        final List<String> newLore = new ArrayList<>();
        newLore.addAll(getRuinsLore(item));
        newLore.addAll(getEnchantsLore(item));
        if (oldLore.size() > 0) {
            newLore.add(ABOVE_LORE ? newLore.size() : 0, SEPARATOR_LINE);
            newLore.addAll(ABOVE_LORE ? newLore.size() : 0, oldLore);
        }
        return setItemDisplay(item, null, newLore, flags.iterator());
    }

    private List<String> trimList(List<String> list) {
        List<String> stringList = new ArrayList<>();
        for (String line: list) stringList.addAll(breakString(line));
        while (!stringList.isEmpty() && stringList.get(0).isEmpty()) stringList.remove(0);
        while (!stringList.isEmpty() && stringList.get(stringList.size() - 1).isEmpty()) stringList.remove(stringList.size() - 1);
        return stringList;
    }
    private List<String> breakString(String string) {
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
    private List<String> getEnchantsLore(ItemStack item) {
        List<String> lore = new ArrayList<>();
        LinkedHashMap<Enchant, Integer> enchants = ENCHANT_MANAGER.extractEnchants(item);

        if (COMPRESSIBLE && enchants.size() > COMPRESS_LIMIT) {
            lore.add(enchants.entrySet().stream().map(enchant -> enchant.getKey().getColoredName(enchant.getValue())).collect(Collectors.joining("§r, ")));
        } else {
            for (Map.Entry<Enchant, Integer> enchant : enchants.entrySet()) {
                lore.add(enchant.getKey().getColoredName(enchant.getValue()));
                if (SHOW_LORE) lore.add("    " + PHManager.translate(enchant.getKey().getLore(enchant.getValue())));
            }
        }
        return lore;
    }
    private List<String> getRuinsLore(ItemStack item) {
        List<String> lore = new ArrayList<>();
        if (RUNE_MANAGER.hasItemRune(item)) {
            Rune rune = RUNE_MANAGER.getItemRune(item);
            lore.add(rune.getColoredName());
            if (SHOW_LORE) lore.add("    " + PHManager.translate(rune.getLore()));
        }

        return lore;
    }

    private final Predicate<Enchant> TREASURE_FILTER = Enchant::isTreasure;
    private final Predicate<Enchant> VANILLA_FILTER = enchant -> (enchant instanceof VanillaEnchant);
    private final BiPredicate<Enchant, ItemStack> AVAILABLE_FILTER = Enchant::canEnchantItem;
    private final BiPredicate<Enchant, ItemStack> EXISTS_FILTER = (enchant, item) -> enchant.getLevel(item) > 0;

    private double calculateEnchantPower(Collection<? extends Map.Entry<Enchant, Integer>> list) {
        return list.stream().mapToDouble(entry -> entry.getValue() * entry.getKey().getMultiplier() * 0.5).sum();
    }
    private double calculateEnchantPower(ItemStack item) {
        return calculateEnchantPower(ENCHANT_MANAGER.extractEnchants(item).entrySet()) + (RUNE_MANAGER.hasItemRune(item) ? 5: 0);
    }
    private double calculateEnchantPower(ItemStack item, Collection<? extends Map.Entry<Enchant, Integer>> list) {
        return calculateEnchantPower(item) + calculateEnchantPower(list);
    }
    public void randomize(ItemStack item, int cost, boolean allowTreasure, boolean filterVanilla) {
        if (item == null || item.getType().equals(Material.AIR)) return; applyItemLore(item);
        if (item.getType().equals(Material.BOOK) && 0.25 <= new Random().nextDouble()) item.setType(Material.ENCHANTED_BOOK);

        List<Enchant> enchants = ENCHANT_MANAGER.STORAGE.getAll();
        if (!allowTreasure) enchants.removeIf(TREASURE_FILTER);
        if (filterVanilla) enchants.removeIf(VANILLA_FILTER);

        Predicate<Enchant> filter = enchant -> !EXISTS_FILTER.test(enchant, item) && AVAILABLE_FILTER.test(enchant, item);
        while (true) {
            double power = calculateEnchantPower(item);
            double chance =  (cost - power) / (double) cost;
            if (chance < new Random().nextDouble()) break;

            List<Enchant> available = enchants.stream().filter(filter).collect(Collectors.toList());
            if (available.size() == 0) break;

            Enchant enchant = available.get(new Random().nextInt(available.size()));
            int level = 1;
            while (level < enchant.getMaxLevel()) {
                double levelPower = calculateEnchantPower(item, Collections.singleton(new Pair<>(enchant, level)));
                double levelChance =  (cost - levelPower) / (double) cost;
                if (levelChance < new Random().nextDouble()) break;
                level += 1;
            }
            enchant.applyEnchant(item, level);
        }

        double power = calculateEnchantPower(item);
        double chance =  (cost - power) / (double) cost;
        List<Rune> runes = RUNE_MANAGER.STORAGE.getAll();
        if (allowTreasure && chance > new Random().nextDouble()) {
            List<Rune> available = runes.stream().filter(rune -> rune.canRuneItem(item)).collect(Collectors.toList());
            if (available.size() == 0) return;

            RandomCollection<Rune> random = new RandomCollection<>(rune -> rune.getRarity().chance, available);
            Rune rune = random.next();
            rune.applyRune(item);
        }
    }
}