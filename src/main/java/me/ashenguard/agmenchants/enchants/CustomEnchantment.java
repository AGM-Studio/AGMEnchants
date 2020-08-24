package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.numeral.RomanInteger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class CustomEnchantment {
    protected BukkitScheduler scheduler = Bukkit.getScheduler();
    protected JavaPlugin plugin = AGMEnchants.getInstance();

    /** ID is the main name of this enchantment, It's not changeable and must be defined in Initialize*/
    protected final String ID;
    /** Version of the enchantment, It will be checked if there is another version available or not*/
    protected final String version;
    /** Name of the enchantment, On enchanting(Every time Item enchantments get sorted) this name is used to show the enchantment on item*/
    protected final String name;
    /** Other names, Name and ID will be added by default,
     * It's possible to change name in middle game and changing name may cause some items remain with old names
     * Enchantment will be recognized with these names */
    protected final List<String> possibleNames;
    /** Description, This description is used in GUI Panel */
    protected final String description;

    /** Treasure, A Treasure is an enchantment that can not be applied with enchantment table. A treasure can be also a cursed one
     * In 3.0 Treasure enchantments can be founded with fishing */
    protected final boolean treasure;
    /** Cursed, A Cursed enchantment means it has mostly negative effect, It's configurable but it's better to not be changed */
    protected final boolean cursed;

    /** The max level of the enchantment, Unsafe enchanting is not allowed with custom enchantments */
    protected final int maxLevel;

    /** The Multipliers used by Enchantment table and Anvil*/
    protected final EnchantmentMultiplier multiplier;
    /** Material Names that this enchantment can be applied */
    protected final List<String> applicable;

    /** The config file for this enchantment */
    protected Configuration config;

    /** This method allow additional checks if there is a requirements, By default it sends true*/
    public boolean canRegister() { return true; }


    /**
     * @param ID The enchantment ID to be saved
     * @param version The enchantment version
     */
    public CustomEnchantment(@NotNull String ID, String version) {
        this.ID = ID;
        this.version = version;

        File configFolder = new File(plugin.getDataFolder(), "configs");
        if (!configFolder.exists()) configFolder.mkdirs();
        saveDefaultConfig();

        this.name = config.getString("Name", ID.replace("_", " "));
        this.possibleNames = config.getStringList("OtherNames");

        if (!possibleNames.contains(name)) possibleNames.add(name);

        this.description = config.getString("Description", "");
        this.applicable = config.getStringList("Applicable");
        this.maxLevel = config.getInt("MaxLevel", 1);
        this.multiplier = new EnchantmentMultiplier(config.getInt("Multiplier.Item", 1), config.getInt("Multiplier.Book", 1));
        this.treasure = config.getBoolean("Treasure", false);
        this.cursed = config.getBoolean("Cursed", false);

        EnchantmentManager.save(this);
    }
    public CustomEnchantment(@NotNull String ID) {
        this(ID, "1.0");
    }

    public boolean isApplicable(Material material) {
        if (material == null) return false;
        if (applicable.contains(material.name())) return true;
        for (String applicableName : applicable) {
            List<String> applicable = AGMEnchants.config.getStringList("ItemsList." + applicableName);
            if (applicable.contains(material.name())) return true;
        }
        return false;
    }
    public boolean canEnchantItem(ItemStack item) {
        if (item == null) return false;
        if (item.getType().equals(Material.ENCHANTED_BOOK)) return true;
        if (!isApplicable(item.getType())) return false;

        for (Enchantment enchantment: item.getEnchantments().keySet())
            if (conflictsWith(enchantment)) return false;

        for (CustomEnchantment enchantment: EnchantmentManager.extractEnchantments(item).keySet())
            if (conflictsWith(enchantment)) return false;

        return true;
    }

    public ItemStack getBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentManager.addEnchantment(book, this, level);
        return book;
    }
    public ItemStack getInfoBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = book.getItemMeta();
        itemMeta.setDisplayName(EnchantmentManager.getColoredName(this));

        List<String> lore = new ArrayList<>(getDescription());
        lore.add("§7§m----------------------");
        lore.add("§6Total Levels: " + getMaxLevel());
        itemMeta.setLore(lore);
        book.setItemMeta(itemMeta);

        return book;
    }
    public ItemStack getInfoBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = book.getItemMeta();
        itemMeta.setDisplayName(EnchantmentManager.getColoredName(this));

        List<String> lore = new ArrayList<>(getDescription());
        lore.add("§7§m----------------------");
        lore.addAll(getLevelDetails(level));
        itemMeta.setLore(lore);
        book.setItemMeta(itemMeta);

        return book;
    }

    protected abstract LinkedHashMap<String, Object> getDefaultConfig();
    protected abstract List<String> getLevelDetails(int level);
    protected abstract boolean conflictsWith(Enchantment enchantment);
    protected abstract boolean conflictsWith(CustomEnchantment enchantment);

    protected void saveDefaultConfig() {
        config = new Configuration(plugin, "Enchants" + File.separatorChar + "configs" + File.separatorChar + ID + ".yml", false);

        if (!config.configFile.exists()) {
            LinkedHashMap<String, Object> defaults = getDefaultConfig();
            for (String path : defaults.keySet())
                if (!config.contains(path)) config.set(path, defaults.get(path));
        }

        config.saveConfig();
    }
    protected void enchanted(ItemStack item) {}
    protected void disenchanted(ItemStack item) {}

    public String getVersion() {
        return version;
    }
    public String getName() {
        return name;
    }
    public List<String> getDescription() { return Arrays.asList(description.split("\n")); }

    public boolean isCursed() {
        return cursed;
    }
    public boolean isTreasure() {
        return treasure;
    }
    public int getMaxLevel() {
        return maxLevel;
    }
    public int getLevel(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getType().equals(Material.ENCHANTED_BOOK)) return 0;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 0;

        for (String line : lore) {
            int level = getLevelFromLine(item, line);
            if (level > 0) return level;
        }

        return 0;
    }
    public int getEnchantLevel(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return 0;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 0;

        for (String line : lore) {
            int level = getLevelFromLine(item, line);
            if (level > 0) return level;
        }

        return 0;
    }
    public int getLevelFromLine(ItemStack item, String line) {
        for (String name : possibleNames)
            if (line.contains(name)) {
                if (maxLevel == 1) return 1;
                return Math.min(RomanInteger.toInteger(line.substring(line.lastIndexOf(" "))), maxLevel);
            }
        return 0;
    }

    public EnchantmentMultiplier getMultiplier() {
        return multiplier;
    }
}
