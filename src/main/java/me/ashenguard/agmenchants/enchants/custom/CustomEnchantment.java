package me.ashenguard.agmenchants.enchants.custom;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.numeral.RomanInteger;
import me.ashenguard.api.utils.FileUtils;
import me.ashenguard.api.utils.Version;
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

public abstract class CustomEnchantment extends CustomEnchantmentDefaultValues {
    protected BukkitScheduler scheduler = Bukkit.getScheduler();
    protected JavaPlugin plugin = AGMEnchants.getInstance();

    protected final String ID; public String getID() { return ID; }
    protected final Version version; public Version getVersion() { return version; }

    protected final String name; public String getName() { return name; }
    protected final List<String> otherNames; public List<String> getOtherNames() { return otherNames; }
    protected final String description; public String getDescription() { return description; }

    protected final List<String> applicable; public List<String> getApplicable() { return applicable; }

    protected final boolean treasure; public boolean isTreasure() { return treasure; }
    protected final boolean cursed; public boolean isCursed() { return cursed; }

    protected final int maxLevel; public int getMaxLevel() { return maxLevel; }

    protected final CustomEnchantmentMultiplier multiplier; public CustomEnchantmentMultiplier getMultiplier() { return multiplier; }

    protected Configuration config;

    public boolean canRegister() { return true; }
    public CustomEnchantment(@NotNull String ID, String version) {
        this(ID, new Version(version));
    }
    public CustomEnchantment(@NotNull String ID, Version version) {
        this.ID = ID;
        this.version = version;

        File configFolder = new File(AGMEnchants.getEnchantsFolder(), "configs");
        if (!configFolder.exists()) configFolder.mkdirs();
        saveDefaultConfig();

        this.name = config.getString("Name", ID.replace("_", " "));
        this.otherNames = config.getStringList("OtherNames");
        if (!otherNames.contains(name)) otherNames.add(name);
        this.description = config.getString("Description", "");
        this.applicable = config.getStringList("Applicable");
        this.maxLevel = config.getInt("MaxLevel", 1);
        this.multiplier = new CustomEnchantmentMultiplier(config.getInt("Multiplier.Item", 1), config.getInt("Multiplier.Book", 1));
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

        List<String> lore = new ArrayList<>(sliceDescription());
        lore.add("§7§m----------------------");
        lore.add("§6Total Levels: " + maxLevel);
        itemMeta.setLore(lore);
        book.setItemMeta(itemMeta);

        return book;
    }
    public ItemStack getInfoBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = book.getItemMeta();
        itemMeta.setDisplayName(EnchantmentManager.getColoredName(this));

        List<String> lore = new ArrayList<>(sliceDescription());
        lore.add("§7§m----------------------");
        lore.addAll(getLevelDetails(level));
        itemMeta.setLore(lore);
        book.setItemMeta(itemMeta);

        return book;
    }

    protected abstract List<String> getLevelDetails(int level);

    public boolean conflictsWith(Enchantment enchantment) {
        return conflicts().contains(enchantment.getName());
    }
    public boolean conflictsWith(CustomEnchantment enchantment) {
        return conflicts().contains(enchantment.getID());
    }

    protected void saveDefaultConfig() {
        config = new Configuration(plugin, "Enchants" + File.separatorChar + "configs" + File.separatorChar + ID + ".yml", false);

        if (!config.configFile.exists()) {
            String string = FileUtils.readStream(AGMEnchants.getInstance().getResource("EnchantConfigExample.yml"));
            FileUtils.writeFile(config.configFile, string);
            config.loadConfig();

            LinkedHashMap<String, Object> defaults = getDefaultConfig();
            for (String path : defaults.keySet())
                config.set(path, defaults.get(path));

            config.saveConfig();
        }

        config.loadConfig();
    }
    public void enchanted(ItemStack item) {}
    public void disenchanted(ItemStack item) {}

    public List<String> sliceDescription() { return Arrays.asList(description.split("\n")); }

    public int getLevel(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getType().equals(Material.ENCHANTED_BOOK)) return 0;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 0;

        for (String line : lore) {
            int level = getLevelFromLine(line);
            if (level > 0) return level;
        }

        return 0;
    }
    public int getEnchantLevel(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return 0;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 0;

        for (String line : lore) {
            int level = getLevelFromLine(line);
            if (level > 0) return level;
        }

        return 0;
    }
    public int getLevelFromLine(String line) {
        for (String name : otherNames)
            if (line.contains(name)) {
                if (maxLevel == 1) return 1;
                return Math.min(RomanInteger.toInteger(line.substring(line.lastIndexOf(" "))), maxLevel);
            }
        return 0;
    }
}
