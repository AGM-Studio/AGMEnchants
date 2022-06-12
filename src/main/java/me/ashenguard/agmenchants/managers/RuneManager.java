package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Rune;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.nbt.NBTCompound;
import me.ashenguard.api.nbt.NBTItem;
import me.ashenguard.api.utils.FileUtils;
import me.ashenguard.exceptions.ConstructorNotFound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class RuneManager {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private static final Messenger MESSENGER = AGMEnchants.getMessenger();

    private static final Configuration config = new Configuration(PLUGIN, "Features/runes.yml");

    private static double fishingChance = 0.005;
    private static double tradeChance = 0.15;
    private static double barterChance = 0.0025;
    private static double lootChance = 0.05;

    public static final Storage STORAGE = new Storage();
    private static final NBT NBT = new NBT();

    public static void loadConfig() {
        fishingChance = config.getDouble("FishingChance", fishingChance * 100) / 100;
        tradeChance = config.getDouble("TradeAdded", tradeChance * 100) / 100;
        barterChance = config.getDouble("BarterChance", barterChance * 100) / 100;
        lootChance = config.getDouble("LootGenerate", lootChance * 100) / 100;
    }

    public static double getFishingChance() {
        return fishingChance;
    }
    public static double getTradeChance() {
        return tradeChance;
    }
    public static double getBarterChance() {
        return barterChance;
    }
    public static double getLootChance() {
        return lootChance;
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void loadRunes() {
        STORAGE.clear();
        for (Rune rune: Loader.getAllRunes()) {
            try {
                rune.register();
            } catch (Throwable throwable) {
                MESSENGER.handleException(throwable);
            }
        }
        STORAGE.sort();

        MESSENGER.Info(String.format("Loaded %d Runes", STORAGE.size()));
    }

    @Nullable public static Rune getItemRune(@NotNull ItemStack item) {
        return NBT.GetRune(item);
    }
    public static boolean setItemRune(@NotNull ItemStack item,@NotNull Rune rune, boolean orig) {
        boolean result = NBT.SetRune(item, rune, orig);
        LoreManager.updateItem(item);
        return result;
    }
    public static boolean setItemRune(@NotNull ItemStack item, @NotNull Rune rune) {
        boolean result = NBT.SetRune(item, rune);
        LoreManager.updateItem(item);
        return result;
    }
    public static boolean delItemRune(@NotNull ItemStack item) {
        boolean result = NBT.RemoveRune(item);
        LoreManager.updateItem(item);
        return result;
    }
    public static boolean hasItemRune(@NotNull ItemStack item) {
        return getItemRune(item) != null;
    }
    public static boolean isItemRune(@NotNull ItemStack item) {
        return NBT.IsOriginalRune(item);
    }

    public static Rune getRandomRune(ItemStack item) {
        List<Rune> runes = STORAGE.getAll();
        if (item != null) runes.removeIf(rune -> !rune.canRuneItem(item));
        return getRandomRune(runes);
    }
    public static Rune getRandomRune() {
        return getRandomRune(STORAGE.getAll());
    }
    public static Rune getRandomRune(List<Rune> runes) {
        double total = runes.stream().map(Rune::getRarity).mapToDouble(Rune.Rarity::getWeight).sum();
        double rnd = new Random().nextDouble() * total;
        for (Rune rune: runes) {
            double chance = rune.getRarity().getWeight();
            if (rnd < chance) return rune;
            else rnd -= chance;
        }
        return runes.get(runes.size() - 1);
    }

    @SuppressWarnings("unused")
    public static class Storage {
        private final List<Rune> list = new ArrayList<>();

        public Rune get(String ID) {
            for(Rune item: list) if (item.ID.equals(ID)) return item;
            return null;
        }
        public Rune get(Rune rune) {
            return get(rune.getName());
        }
        public Rune get(Object object) {
            return get(String.valueOf(object));
        }

        public List<Rune> getAll() {
            return new ArrayList<>(list);
        }

        public void clear() {
            list.clear();
        }

        public boolean save(Rune rune) {
            list.remove(get(rune.ID));
            return list.add(rune);
        }

        public boolean saveIfAbsent(Rune rune) {
            if (get(rune.ID) == null) return save(rune);
            return false;
        }

        public void sort() {
            list.sort(Comparator.comparing(e -> e.ID));
        }

        public int size() {
            return list.size();
        }

        public void remove(Rune rune) {
            list.remove(rune);
        }
    }
    private static class NBT {
        private static final String NBT_TAG_RUNE = "Rune";
        private static final String NBT_TAG_ORIG = "Original";
        private static final String NBT_TAG_NAME = "ID";

        private boolean RemoveRune(ItemStack item) {
            if (item == null || item.getType().equals(Material.AIR)) return false;
            NBTItem nbt = new NBTItem(item, true);
            nbt.removeKey(NBT_TAG_RUNE);
            return true;
        }
        private NBTCompound ExtractRune(ItemStack item) {
            if (item == null || item.getType().equals(Material.AIR)) return null;
            NBTItem nbt = new NBTItem(item, true);
            return nbt.hasKey(NBT_TAG_RUNE) ? nbt.getCompound(NBT_TAG_RUNE) : nbt.addCompound(NBT_TAG_RUNE);
        }
        private Rune GetRune(ItemStack item) {
            if (item == null || item.getType().equals(Material.AIR)) return null;
            NBTItem nbt = new NBTItem(item, true);
            if (!nbt.hasKey(NBT_TAG_RUNE)) return null;
            NBTCompound compound = nbt.getCompound(NBT_TAG_RUNE);
            return STORAGE.get(compound.getString(NBT_TAG_NAME));
        }
        private boolean SetRune(ItemStack item, Rune rune) {
            return SetRune(item, rune, false);
        }
        private boolean SetRune(ItemStack item, Rune rune, boolean orig) {
            if (item == null || item.getType().equals(Material.AIR)) return false;
            if (rune == null) RemoveRune(item);
            else {
                NBTCompound compound = ExtractRune(item);
                if (compound == null) return false;
                compound.setString(NBT_TAG_NAME, rune.ID);
                compound.setBoolean(NBT_TAG_ORIG, orig);
            }
            return true;
        }
        private boolean IsOriginalRune(ItemStack item) {
            if (item == null || item.getType().equals(Material.AIR)) return false;
            NBTCompound nbt = ExtractRune(item);
            return nbt != null && nbt.hasKey(NBT_TAG_ORIG) && nbt.getBoolean(NBT_TAG_ORIG);
        }
    }
    private static class Loader {
        private static final File RUNES_FOLDER = new File(PLUGIN.getDataFolder(), "Runes");
        static {
            if (!RUNES_FOLDER.exists() && RUNES_FOLDER.mkdirs()) MESSENGER.Debug("General", "Rune folder wasn't found, A new one created");
        }

        private static boolean isRuneEnabled(Rune rune) {
            return rune != null && rune.getName() != null && rune.canBeRegistered();
        }
        public static Rune getRune(String fileName) {
            List<Class<?>> subs = FileUtils.getClasses(RUNES_FOLDER, fileName, Rune.class);
            if (subs == null || subs.isEmpty()) return null;

            File JAR = new File(RUNES_FOLDER, fileName);
            Rune rune = createInstance(subs.get(0), JAR);
            try {
                if (isRuneEnabled(rune)) return rune;
            } catch (Exception exception) {
                MESSENGER.Warning("Unable to register rune called " + rune.getName());
                MESSENGER.handleException(exception);
            }
            return null;
        }
        public static List<Rune> getAllRunes() {
            List<Rune> runes = new ArrayList<>();
            for (String filename: RUNES_FOLDER.list()) {
                Rune rune = getRune(filename);
                if (rune != null) runes.add(rune);
            }
            return runes;
        }
        private static Rune createInstance(Class<?> clazz, File JAR) {
            if (clazz == null) return null;
            if (!Rune.class.isAssignableFrom(clazz)) return null;
            Rune rune = null;
            Class<? extends Rune> runeClass = clazz.asSubclass(Rune.class);

            try {
                Constructor<?>[] constructors = runeClass.getConstructors();
                if (constructors.length == 0) {
                    throw new ConstructorNotFound(runeClass);
                } else {
                    for (Constructor<?> constructor : constructors) {
                        if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(File.class)) {
                            rune = (Rune) constructor.newInstance(JAR);
                        }
                    }
                }
            } catch (Throwable throwable) {
                MESSENGER.Warning(String.format("Failed to load rune from class named %s (%s)", runeClass.getSimpleName(), runeClass.getName()));
                MESSENGER.handleException(throwable, "RuneLoader_Exception");
            }
            return rune;
        }
    }
}
