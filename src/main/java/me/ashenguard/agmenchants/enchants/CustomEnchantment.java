package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.messenger.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class CustomEnchantment {
    protected final boolean treasure;
    protected final boolean cursed;
    protected final int maxLevel;
    protected final int bookMultiplier;
    protected final int itemMultiplier;
    protected final String version;

    protected File configFile;
    protected YamlConfiguration config;


    public void loadConfig() {
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException exception) {
            Messenger.handleException(exception);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException exception) {
            Messenger.handleException(exception);
        }
    }

    protected BukkitScheduler scheduler = Bukkit.getScheduler();
    protected JavaPlugin plugin = AGMEnchants.getInstance();

    protected String name;
    protected String description;

    protected List<String> applicable;

    public CustomEnchantment(String name, String version) {
        File configFolder = new File(AGMEnchants.getEnchantsFolder(), "configs");
        if (!configFolder.exists() && configFolder.mkdirs())
            Messenger.Debug("General", "Config folder wasn't found, A new one created");

        configFile = new File(configFolder, name + ".yml");
        config = configFile.exists() ? YamlConfiguration.loadConfiguration(configFile) : new YamlConfiguration();
        HashMap<String, Object> defaults = getDefaultConfig();
        for (String path : defaults.keySet())
            if (!config.contains(path)) config.set(path, defaults.get(path));

        saveConfig();
        loadConfig();

        this.version = version;
        this.name = name;
        this.description = config.getString("Description", "");
        this.applicable = config.getStringList("Applicable");
        this.maxLevel = config.getInt("MaxLevel", 1);
        this.itemMultiplier = config.getInt("Multiplier.Item", 1);
        this.bookMultiplier = config.getInt("Multiplier.Book", 1);
        this.treasure = config.getBoolean("Treasure", false);
        this.cursed = config.getBoolean("Cursed", false);
        EnchantmentManager.save(this);
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
        lore.add("§m----------------------");
        lore.add("Levels: " + getMaxLevel());
        itemMeta.setLore(lore);
        book.setItemMeta(itemMeta);

        return book;
    }
    public ItemStack getInfoBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = book.getItemMeta();
        itemMeta.setDisplayName(EnchantmentManager.getColoredName(this));

        List<String> lore = new ArrayList<>(getDescription());
        lore.add("§m----------------------");
        lore.addAll(getLevelDetails(level));
        itemMeta.setLore(lore);
        book.setItemMeta(itemMeta);

        return book;
    }

    protected abstract HashMap<String, Object> getDefaultConfig();
    protected abstract List<String> getLevelDetails(int level);
    protected abstract boolean conflictsWith(Enchantment enchantment);
    protected abstract boolean conflictsWith(CustomEnchantment enchantment);

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
    public int getItemMultiplier() {
        return itemMultiplier;
    }
    public int getBookMultiplier() {
        return bookMultiplier;
    }
}
