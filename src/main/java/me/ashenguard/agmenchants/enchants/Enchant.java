package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.LoreManager;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.utils.encoding.Roman;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "unused", "EmptyMethod"})
public abstract class Enchant extends Enchantment {
    protected final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    protected final Messenger MESSENGER = AGMEnchants.getMessenger();

    protected final Configuration config;

    private final String name;
    private final String description;
    private final List<String> lore;
    private final Multiplier multiplier;

    public boolean register() {
        Enchant exists = EnchantManager.STORAGE.get(this.getKey());
        if (exists != null) return false;
        EnchantManager.STORAGE.save(this);
        MESSENGER.Debug("Enchants", "Enchantment has been registered.", "Enchantment= §6" + toString());
        return true;
    }

    public abstract void unregister();

    public Enchant(@NotNull NamespacedKey key, Configuration config) {
        super(key);
        this.config = config;

        this.name = PHManager.translate(config.getString("Name", key.getKey()));
        this.description = PHManager.translate(config.getString("Description", ""));

        List<String> list = config.getStringList("Lore");
        if (list.size() == 0)  list = Collections.singletonList(description);
        this.lore = PHManager.translate(list);

        this.multiplier = new Enchant.Multiplier(config.getInt("Multiplier", 1));
    }

    public abstract void onEnchanting(ItemStack item, int level);
    public abstract void onDisenchanting(ItemStack item, int level);
    public abstract List<Placeholder> getPlaceholders(int level);
    public List<Placeholder> getPlaceholders(ItemStack item) {
        return getPlaceholders(getLevel(item));
    }

    public @NotNull String getName() {
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

    public abstract boolean isApplicable(Material material);
    public abstract boolean conflictsWith(@NotNull Enchant enchant);
    @Override public boolean conflictsWith(@NotNull Enchantment enchant) {
        return conflictsWith(EnchantManager.STORAGE.get(enchant));
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

    public boolean canEnchantItem(@NotNull ItemStack item) {
        if (item.getType().equals(Material.AIR)) return false;
        if (item.getType().equals(Material.ENCHANTED_BOOK)) return true;
        if (!isApplicable(item.getType())) return false;

        Set<Enchant> enchants = EnchantManager.extractEnchants(item).keySet();
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
        String lvl = Roman.to(level);
        return String.format("%s %s%s", name, EnchantManager.getLevelColor(lvl), lvl);
    }

    public int applyEnchant(ItemStack item, int level) {
        return EnchantManager.setItemEnchant(item, this, level);
    }
    public int removeEnchant(ItemStack item) {
        return EnchantManager.delItemEnchant(item, this);
    }

    public int getLevel(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getType().equals(Material.ENCHANTED_BOOK)) return 0;
        return EnchantManager.getItemEnchant(item, this);
    }

    public ItemStack getEnchantedBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        this.applyEnchant(book, level);
        return book;
    }

    public ItemStack getInfoBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        List<String> lore = Arrays.asList(description.split("\n"));
        return LoreManager.setItemDisplay(book, getColoredName(), lore, null);
    }

    @Override public String toString() {
        return String.format("Enchantment[ID=%s, Name=%s]", getKey(), getName());
    }

    public abstract boolean canBeTraded();
    public abstract boolean canBeBartered();
    public abstract boolean canBeFished();
    public abstract boolean canBeLooted(World world);

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

        @SuppressWarnings("deprecation")
        public static Rarity get(Enchant enchant) {
            return enchant.isCursed() ? CURSED : enchant.isTreasure() ? TREASURE : NORMAL;
        }

        public final String color;
        Rarity(String color) {
            this.color = color;
        }

        Rarity(String path, String def) {
            Configuration config = EnchantManager.getConfig();

            this.color = PHManager.translate(config.getString(String.format("Colors.%s", getCapitalizedName()), def));
        }

        public String getCapitalizedName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }
}
