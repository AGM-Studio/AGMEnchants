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
import java.util.*;

public abstract class CustomEnchantment extends CustomEnchantmentDefaultValues implements Comparable<CustomEnchantment> {
    protected BukkitScheduler scheduler = Bukkit.getScheduler();
    protected JavaPlugin plugin = AGMEnchants.getInstance();

    protected final String ID;
    protected final Version version;
    protected final String name;
    protected final List<String> otherNames;
    protected final String description;
    protected final List<String> applicable;
    protected final List<String> conflicts;
    protected final boolean treasure;
    protected final boolean cursed;
    protected final int maxLevel;
    protected final CustomEnchantmentMultiplier multiplier;

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
        this.conflicts = config.getStringList("Conflicts");
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
        if (getApplicable().contains(material.name())) return true;
        for (String applicableName : getApplicable()) {
            List<String> applicable = AGMEnchants.config.getStringList("ItemsList." + applicableName);
            if (applicable.contains(material.name())) return true;
        }
        return false;
    }
    public boolean canEnchantItem(ItemStack item) {
        if (item == null) return false;
        if (item.getType().equals(Material.ENCHANTED_BOOK)) return true;
        if (!isApplicable(item.getType())) return false;

        for (Enchantment enchantment: item.getEnchantments().keySet()) {
            if (conflictsWith(enchantment)) return false;
        }

        for (CustomEnchantment enchantment: EnchantmentManager.extractEnchantments(item).keySet()) {
            if (conflictsWith(enchantment)) return false;
            if (enchantment.conflictsWith(this)) return false;
        }

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
        itemMeta.setDisplayName(getColoredName());
        itemMeta.setLore(sliceDescription());
        book.setItemMeta(itemMeta);

        return book;
    }
    public ItemStack getInfoBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = book.getItemMeta();
        itemMeta.setDisplayName(getColoredName());

        List<String> lore = getLevelDetails(level);
        itemMeta.setLore(lore);
        book.setItemMeta(itemMeta);

        return book;
    }

    public String getColoredName() {
        String color = isTreasure() ? "§b" : "§7";
        color = isCursed() ? "§c" : color;

        return color + getName();
    }
    public String getColoredName(int level) {
        String name = getColoredName();

        if (getMaxLevel() == 1) return name;
        return name + " " + RomanInteger.toRoman(Math.min(level, getMaxLevel()));
    }

    protected abstract CustomEnchantmentLevel getCustomEnchantmentLevel(int level);
    public List<String> getLevelDetails(Map<String, Object> details) {
        details = details == null ? new LinkedHashMap<>() : details;
        String levelInfo = config.getString("LevelInfo");
        for(Map.Entry<String, Object> detail: details.entrySet())
            levelInfo = levelInfo.replace("%" + detail.getKey() + "%", String.valueOf(detail.getValue()));

        return new ArrayList<>(Arrays.asList(levelInfo.split("\n")));
    }
    public List<String> getLevelDetails(int level) {
        CustomEnchantmentLevel customLevel = getCustomEnchantmentLevel(level);
        LinkedHashMap<String, Object> details = customLevel == null? new LinkedHashMap<>() : customLevel.getLevelDetail();

        return getLevelDetails(details);
    }

    public boolean conflictsWith(Enchantment enchantment) {
        return getConflicts().contains(enchantment.getName());
    }
    public boolean conflictsWith(CustomEnchantment enchantment) {
        return getConflicts().contains(enchantment.getID());
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
    public void enchanted(ItemStack item, int level) {}
    public void disenchanted(ItemStack item) {}

    public List<String> sliceDescription() { return Arrays.asList(getDescription().split("\n")); }

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
    public int getEnchantmentLevel(ItemStack item) {
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
        for (String name : getOtherNames())
            if (line.contains(name)) {
                if (maxLevel == 1) return 1;
                return Math.min(RomanInteger.toInteger(line.substring(line.lastIndexOf(" "))), maxLevel);
            }
        return 0;
    }

    @Override
    public int compareTo(@NotNull CustomEnchantment o) {
        return ID.compareTo(o.ID);
    }

    public String getID() {
        return ID;
    }
    public Version getVersion() {
        return version;
    }
    public String getName() {
        return name;
    }
    public List<String> getOtherNames() {
        return otherNames;
    }
    public String getDescription() {
        return description;
    }
    public List<String> getApplicable() {
        return applicable;
    }
    public List<String> getConflicts() {
        return conflicts;
    }
    public boolean isTreasure() {
        return treasure;
    }
    public boolean isCursed() {
        return cursed;
    }
    public int getMaxLevel() {
        return maxLevel;
    }
    public CustomEnchantmentMultiplier getMultiplier() {
        return multiplier;
    }
}
