package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.utils.encoding.Roman;
import me.ashenguard.api.utils.extra.MemorySectionReader;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "unused", "EmptyMethod"})
public abstract class Enchant {
    protected final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    protected final Messenger MESSENGER = AGMEnchants.getMessenger();
    protected static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();

    public final String ID;

    protected final Configuration config;

    private final String name;
    private final String description;
    private final List<String> lore;
    private final List<String> applicable;
    private final List<String> conflicts;
    private final Multiplier multiplier;
    private final boolean treasure;
    private final boolean cursed;
    private final int maxLevel;

    public boolean register() {
        Enchant exists = ENCHANT_MANAGER.STORAGE.get(ID);
        if (exists != null) return false;
        ENCHANT_MANAGER.STORAGE.save(this);
        MESSENGER.Debug("Enchants", "Enchantment has been registered.", "Enchantment= §6" + toString());
        return true;
    }

    public Enchant(String ID, Configuration config) {
        this.ID = ID;
        this.config = config;

        ConfigReader reader = new ConfigReader(config);

        this.name = reader.readName(ID);
        this.description = reader.readDescription();
        this.lore = reader.readLore();
        this.applicable = reader.readApplicable();
        this.conflicts = reader.readConflicts();
        this.maxLevel = reader.readMaxLevel();
        this.multiplier = reader.readMultiplier();
        this.treasure = reader.isTreasure();
        this.cursed = reader.isCursed();
    }

    public abstract void onEnchanting(ItemStack item, int level);
    public abstract void onDisenchanting(ItemStack item, int level);
    public abstract List<Placeholder> getPlaceholders(int level);
    public List<Placeholder> getPlaceholders(ItemStack item) {
        return getPlaceholders(getLevel(item));
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getLore(int level) {
        String line;
        try {
            line = lore.get(level - 1);
        } catch (IndexOutOfBoundsException ignored) {
            line = lore.get(0);
        }
        List<Placeholder> placeholders = new ArrayList<>(getPlaceholders(level));
        placeholders.add(new Placeholder("Level", (player, s) -> String.valueOf(level)));
        for (Placeholder placeholder: placeholders) line = placeholder.apply(line, null);
        return line;
    }
    public Rarity getRarity() {
        return Rarity.get(this);
    }

    private final ConfigurationSection listOfItems = AGMEnchants.getItemsList();
    private boolean isApplicable(String material, String applicable) {
        if (applicable.equalsIgnoreCase(material)) return true;
        List<String> list = listOfItems.getStringList(applicable);
        for (String name: list) if (isApplicable(material, name)) return true;
        return false;
    }
    public boolean isApplicable(Material material) {
        if (material == null || material.equals(Material.AIR)) return false;
        if (applicable.contains("EVERYTHING")) return true;
        for (String name: applicable) if (isApplicable(material.name(), name)) return true;
        return false;
    }
    public boolean conflictsWith(Enchant enchant) {
        return conflicts.contains(enchant.ID) || enchant.conflicts.contains(ID);
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
    public int getMultiplier() {
        return multiplier.get();
    }
    public int getMultiplier(ItemStack item) {
        return multiplier.get(item);
    }
    public int getMultiplier(boolean book) {
        return multiplier.get(book);
    }

    public boolean canEnchantItem(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        if (item.getType().equals(Material.ENCHANTED_BOOK)) return true;
        if (!isApplicable(item.getType())) return false;

        Set<Enchant> enchants = ENCHANT_MANAGER.extractEnchants(item).keySet();
        for (Enchant enchant: enchants)
            if (conflictsWith(enchant)) return false;

        return true;
    }

    public boolean isSafe(int level) {
        return 0 < level && level <= getMaxLevel();
    }

    public String getColor() {
        return getRarity().color;
    }
    public String getColoredName() {
        return getColor() + getName();
    }
    public String getColoredName(int level) {
        String name = getColoredName();

        if (getMaxLevel() == 1 && level == 1) return name;
        return String.format("%s %s", name, Roman.to(level));
    }

    public int applyEnchant(ItemStack item, int level) {
        return ENCHANT_MANAGER.setItemEnchant(item, this, level);
    }
    public boolean removeEnchant(ItemStack item) {
        return ENCHANT_MANAGER.delItemEnchant(item, this);
    }

    public int getLevel(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getType().equals(Material.ENCHANTED_BOOK)) return 0;
        return ENCHANT_MANAGER.getItemEnchant(item, this);
    }

    public ItemStack getEnchantedBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        this.applyEnchant(book, level);
        return book;
    }

    public ItemStack getInfoBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        List<String> lore = Arrays.asList(description.split("\n"));
        return AGMEnchants.getItemManager().setItemNameLore(book, getColoredName(), lore);
    }

    @Override public String toString() {
        return String.format("%s[ID=%s, Name=%s]", "Enchant", ID, getName());
    }

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
        private List<String> readBareLore() {
            List<String> list = readStringList("Lore");
            if (list != null && list.size() > 0) return list;

            return Collections.singletonList(readDescription());
        }
        public List<String> readLore() {
            return PHManager.translate(readBareLore());
        }
        public boolean isTreasure() {
            return readBoolean(false,"Treasure");
        }
        public boolean isCursed() {
            return readBoolean(false,"Cursed");
        }
        public int readMaxLevel() {
            return readInt(1, "MaxLevel");
        }
        public List<String> readApplicable() {
            return readStringList("Applicable");
        }
        public List<String> readConflicts() {
            return readStringList("Conflicts");
        }
        public Enchant.Multiplier readMultiplier() {
            return new Enchant.Multiplier(readInt(1, "Multiplier"));
        }
    }
    public static class Multiplier {
        private final int multiplier;

        protected Multiplier(int multiplier) {
            this.multiplier = Math.max(multiplier, 1);
        }
        protected Multiplier(Configuration config) {
            this(config.getInt("Multiplier", 1));
        }

        public int get(ItemStack item) {
            return get(item != null && item.getType().equals(Material.ENCHANTED_BOOK));
        }
        public int get(boolean book) {
            return book ? (multiplier + 1) / 2 : multiplier;
        }
        public int get() {
            return get(false);
        }
    }

    public enum Rarity {
        NORMAL("Colors.Enchants.Normal", "§7"),
        CURSED("Colors.Enchants.Cursed", "§c"),
        TREASURE("Colors.Enchants.Treasure", "§b");

        public static Rarity get(Enchant enchant) {
            return enchant.isCursed() ? CURSED : enchant.isTreasure() ? TREASURE : NORMAL;
        }

        public final String color;
        Rarity(String color) {
            this.color = color;
        }

        Rarity(String path, String def) {
            this(PHManager.translate(AGMEnchants.getConfiguration().getString(path, def)));
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
