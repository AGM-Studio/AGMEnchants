package me.ashenguard.agmenchants.runes;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.ItemMaker;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.spigot.SpigotPlugin;
import me.ashenguard.api.utils.FileUtils;
import me.ashenguard.api.utils.extra.MemorySectionReader;
import me.ashenguard.api.versions.Version;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "unused", "EmptyMethod", "SameReturnValue"})
public abstract class Rune implements Listener {
    protected final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    protected final Messenger MESSENGER = AGMEnchants.getMessenger();
    protected static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();

    private final Version version;

    private static Configuration getConfiguration(String ID, File JAR) {
        SpigotPlugin plugin = AGMEnchants.getInstance();
        String path = String.format("Runes/configs/%s.yml", ID);
        InputStream stream = FileUtils.getResource(JAR, "config.yml");
        return new Configuration(plugin, path, stream);
    }

    public final String ID;

    protected final Configuration config;

    private final String name;
    private final String description;
    private final String lore;
    private final List<String> applicable;
    private final Rarity rarity;

    private final ItemStack item;

    public boolean canBeRegistered() {
        return true;
    }
    public boolean register() {
        Rune exists = RUNE_MANAGER.STORAGE.get(ID);
        if (exists != null) return false;
        RUNE_MANAGER.STORAGE.save(this);
        Bukkit.getPluginManager().registerEvents(this, PLUGIN);
        MESSENGER.Debug("Runes", "Rune has been registered.", "Rune= §6" + toString());
        return true;
    }

    public Rune(File JAR) {
        this(JAR, JAR.getName().substring(0, JAR.getName().lastIndexOf('.')));
    }
    public Rune(File JAR, String ID) {
        this(JAR, ID, "1.0");
    }
    public Rune(File JAR, String ID, String version) {
        this(JAR, ID, new Version(version));
    }
    public Rune(File JAR, String ID, Version version) {
        this(ID, getConfiguration(ID, JAR), version);
    }

    private Rune(String ID, Configuration config, Version version) {
        this.ID = ID;
        this.version = version;
        this.config = config;

        ConfigReader reader = new ConfigReader(config);

        this.name = reader.readName(ID);
        this.description = reader.readDescription();
        this.lore = reader.readLore();
        this.applicable = reader.readApplicable();
        this.rarity = reader.readRarity();
        this.item = reader.readItem();

        AGMEnchants.getItemManager().setItemDisplay(this.item, getColoredName(), null, null);
    }

    public abstract List<Placeholder> getPlaceholders(ItemStack item);

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getLore() {
        return lore;
    }
    public Rarity getRarity() {
        return rarity;
    }

    private boolean isApplicable(String material, String applicable) {
        if (applicable.equalsIgnoreCase(material)) return true;
        List<String> list = AGMEnchants.getMainManager().getGroups().getStringList(applicable);
        for (String name: list) if (isApplicable(material, name)) return true;
        return false;
    }
    public boolean isApplicable(Material material) {
        if (material == null || material.equals(Material.AIR)) return false;
        if (applicable.contains("EVERYTHING")) return true;
        for (String name: applicable) if (isApplicable(material.name(), name)) return true;
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canRuneItem(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        if (RUNE_MANAGER.hasItemRune(item)) return false;
        return isApplicable(item.getType());
    }

    public String getColor() {
        return getRarity().color;
    }
    public String getColoredName() {
        return getColor() + getName();
    }

    public boolean applyOriginalRune(ItemStack item) {
        return RUNE_MANAGER.setItemRune(item, this, true);
    }
    public boolean applyRune(ItemStack item) {
        return RUNE_MANAGER.setItemRune(item, this);
    }
    public boolean removeRune(ItemStack item) {
        if (RUNE_MANAGER.getItemRune(item).equals(this)) return RUNE_MANAGER.delItemRune(item);
        else return false;
    }
    public boolean hasRune(ItemStack item) {
        return this.equals(RUNE_MANAGER.getItemRune(item));
    }

    public ItemStack getRune() {
        ItemStack item = this.item.clone();
        this.applyOriginalRune(item);
        return item;
    }


    public boolean isRuneItem(ItemStack item) {
        return getRune().isSimilar(item);
    }

    public ItemStack getInfoItem() {
        ItemStack item = this.item.clone();
        List<String> lore = Arrays.asList(description.split("\n"));
        return AGMEnchants.getItemManager().setItemDisplay(item, getColoredName(), lore, null);
    }

    @Override public String toString() {
        return String.format("%s[ID=%s, Name=%s]", "Rune", ID, getName());
    }

    public Version getVersion() {
        return version;
    }

    public void onRuneApply(ItemStack result) {}

    private static class ConfigReader extends MemorySectionReader {
        public ConfigReader(MemorySection memory) {
            super(memory);
        }

        public String readName(String ID) {
            return PHManager.translate(readString(ID, 0, "Name"));
        }
        public String readDescription() {
            return PHManager.translate(readString("", 0, "Description", "Desc"));
        }
        public String readLore() {
            return PHManager.translate(readString(readDescription(), 0, "Lore"));
        }
        public List<String> readApplicable() {
            return readStringList("Applicable");
        }
        public Rarity readRarity() {
            return Rarity.get(readString("Common", 0, "Rarity"));
        }
        public ItemStack readItem() {
            String texture = readString("", 0, "Texture", "Value");
            String UUID = readString("", 0, "UUID");
            return ItemMaker.getCustomHead(UUID, texture);
        }
    }

    public enum Rarity {
        COMMON("§7", 54, 1, 20),
        UNCOMMON("§a", 30, 2, 50),
        RARE("§b", 10, 4, 75),
        EPIC("§d", 5, 6, 90),
        LEGENDARY("§6", 1, 8, 100);

        public final String color;
        public final double weight;
        public final double chance;
        public final int cost;

        Rarity(String color, double weight, int cost, double chance) {
            Configuration config = RUNE_MANAGER.getConfig();
            String name = getCapitalizedName();

            this.color = PHManager.translate(config.getString(String.format("Color.%s", name), color));
            this.weight = config.getDouble(String.format("Weight.%s", name), weight);
            this.chance = Math.max(0, Math.min(1, config.getDouble(String.format("ExtractChance.%s", name), chance) / 100));
            this.cost = cost;
        }

        public double getChance() {
            return chance;
        }
        public double getWeight() {
            return weight;
        }
        public int getCost() {
            return cost;
        }

        public String getCapitalizedName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }

        protected static Rarity get(String string) {
            if (Rarity.COMMON.name().equalsIgnoreCase(string)) return Rarity.COMMON;
            if (Rarity.UNCOMMON.name().equalsIgnoreCase(string)) return Rarity.UNCOMMON;
            if (Rarity.RARE.name().equalsIgnoreCase(string)) return Rarity.RARE;
            if (Rarity.EPIC.name().equalsIgnoreCase(string)) return Rarity.EPIC;
            if (Rarity.LEGENDARY.name().equalsIgnoreCase(string)) return Rarity.LEGENDARY;
            return COMMON;
        }
    }
}
