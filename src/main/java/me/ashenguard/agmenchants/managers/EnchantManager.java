package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.CustomEnchant;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.VanillaEnchant;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.utils.FileUtils;
import me.ashenguard.api.utils.Filter;
import me.ashenguard.api.utils.Pair;
import me.ashenguard.exceptions.ConstructorNotFound;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@SuppressWarnings({"UnusedReturnValue", "deprecation", "unused"})
public class EnchantManager {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private static final Messenger MESSENGER = AGMEnchants.getMessenger();

    private static final File ENCHANTS_FOLDER = new File(PLUGIN.getDataFolder(), "Enchants");

    private static final Map<NamespacedKey, Enchantment> KEY_ENCHANTMENT_MAP;
    private static final Map<String, Enchantment> NAME_ENCHANTMENT_MAP;
    static {
        if (!ENCHANTS_FOLDER.exists() && ENCHANTS_FOLDER.mkdirs()) MESSENGER.Debug("General", "Enchants folder wasn't found, A new one created");

        Map<NamespacedKey, Enchantment> KEY_MAP = new HashMap<>();
        Map<String, Enchantment> NAME_MAP = new HashMap<>();
        try {
            Field byKeyField = Enchantment.class.getDeclaredField("byKey");
            Field byNameField = Enchantment.class.getDeclaredField("byName");
            byKeyField.setAccessible(true);
            byNameField.setAccessible(true);
            KEY_MAP = (Map<NamespacedKey, Enchantment>) byKeyField.get(null);
            NAME_MAP = (Map<String, Enchantment>) byNameField.get(null);
        } catch (Throwable throwable) {
            MESSENGER.handleException(throwable);
        }

        KEY_ENCHANTMENT_MAP = KEY_MAP;
        NAME_ENCHANTMENT_MAP = NAME_MAP;
    }

    private final Configuration config;
    private final Map<String, String> LEVEL_COLOR;
    private final String LEVEL_COLOR_DEFAULT;
    private final boolean LEVEL_COLOR_ENABLED;
    public final boolean REVERSE_ENCHANT;

    public final Storage STORAGE = new Storage();

    public EnchantManager() {
        config = new Configuration(PLUGIN, "Features/runes.yml");

        REVERSE_ENCHANT = config.getBoolean("ReverseEnchant", false);
        ConfigurationSection section = config.getConfigurationSection("Colors.Levels");
        LEVEL_COLOR = new HashMap<>();
        if (section == null) {
            LEVEL_COLOR_ENABLED = false;
            LEVEL_COLOR_DEFAULT = "";
        } else {
            LEVEL_COLOR_ENABLED = section.getBoolean("Enable", false);
            LEVEL_COLOR_DEFAULT = section.getString("Default",  "");
            for (String key: section.getKeys(false)) {
                if (key.equals("Enable") || key.equals("Default")) continue;
                LEVEL_COLOR.put(key, section.getString(key, LEVEL_COLOR_DEFAULT));
            }
        }
    }

    public Configuration getConfig() {
        return config;
    }
    public String getLevelColor(String level) {
        if (!LEVEL_COLOR_ENABLED) return "";
        return LEVEL_COLOR.getOrDefault(level, LEVEL_COLOR_DEFAULT);
    }

    public void loadEnchants() {
        try {
            Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
            acceptingNew.setAccessible(true);
            acceptingNew.set(null, true);
        } catch (Exception exception) {
            MESSENGER.handleException(exception);
        }

        STORAGE.clear();
        for (Enchantment enchantment:Enchantment.values()) {
            if (enchantment.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) || enchantment.getKey().getNamespace().equals(NamespacedKey.BUKKIT))
                new VanillaEnchant(enchantment).register();
            else
                unregisterEnchantment(enchantment);
        }
        for (CustomEnchant enchant: Loader.getAllEnchantments()) {
            try {
                enchant.register();
            } catch (Throwable throwable) {
                MESSENGER.handleException(throwable);
            }
        }

        STORAGE.sort();
        Enchantment.stopAcceptingRegistrations();
        MESSENGER.Info(String.format("Loaded %d enchantments(Including the vanilla)", STORAGE.size()));
    }

    public void unregisterEnchantment(Enchantment enchantment) {
        if (enchantment.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) || enchantment.getKey().getNamespace().equals(NamespacedKey.BUKKIT)) return;
        try {
            KEY_ENCHANTMENT_MAP.remove(enchantment.getKey());
            NAME_ENCHANTMENT_MAP.remove(enchantment.getName());
        } catch (Throwable ignored) {}
    }

    public HashMap<Enchant, Integer> extractEnchants(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return new HashMap<>();
        Map<Enchantment, Integer> enchants = new HashMap<>(item.getEnchantments());
        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            Map<Enchantment, Integer> storages = ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();
            for (Map.Entry<Enchantment, Integer> enchant: storages.entrySet()) {
                if (enchant.getValue() == 0) continue;
                enchants.computeIfPresent(enchant.getKey(), (e, l) -> Math.max(l, enchant.getValue()));
                enchants.putIfAbsent(enchant.getKey(), enchant.getValue());
            }
        }
        return STORAGE.translate(enchants);
    }
    public int getItemEnchant(ItemStack item, Enchant enchant) {
        return item.getEnchantmentLevel(enchant);
    }
    public int setItemEnchantAndLore(ItemStack item, Enchant enchant, int level) {
        if (enchant == null || !enchant.isSafe(level)) return -1;
        AGMEnchants.getItemManager().secureItemLore(item);
        int oldLevel = setItemEnchant(item, enchant, level);
        AGMEnchants.getItemManager().applyItemLore(item);
        return oldLevel;
    }
    public int setItemEnchant(ItemStack item, Enchant enchant, int level) {
        if (enchant == null || !enchant.isSafe(level)) return -1;
        int oldLevel = getItemEnchant(item, enchant);
        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(enchant, level, true);
            item.setItemMeta(meta);
        } else if (item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(enchant, level, true);
            item.setItemMeta(meta);
        }
        return oldLevel;
    }
    public int delItemEnchant(ItemStack item, Enchant enchant) {
        int result = item.removeEnchantment(enchant);
        AGMEnchants.getItemManager().applyItemLore(item);
        return result;
    }
    public void clearItemEnchants(ItemStack item) {
        for (Enchant enchant: extractEnchants(item).keySet()) item.removeEnchantment(enchant);
        AGMEnchants.getItemManager().applyItemLore(item);
    }
    public boolean addItemEnchant(ItemStack item, Enchant enchant, int level) {
        if (getItemEnchant(item, enchant) > 0) return false;
        return setItemEnchant(item, enchant, level) > 0;
    }

    private double calculateEnchantWeight(Collection<? extends Map.Entry<Enchant, Integer>> list) {
        return list.stream().mapToDouble(entry -> entry.getValue() * entry.getKey().getMultiplier()).sum();
    }
    private double calculateEnchantWeight(ItemStack item) {
        return calculateEnchantWeight(extractEnchants(item).entrySet());
    }
    private double calculateEnchantWeight(ItemStack item, Collection<? extends Map.Entry<Enchant, Integer>> list) {
        return calculateEnchantWeight(item) + calculateEnchantWeight(list);
    }

    @Nullable public Pair<Enchant, Integer> getRandomEnchant(@NotNull ItemStack item, List<Enchant> enchants, int power) {
        Predicate<Enchant> filter = enchant -> item.getEnchantmentLevel(enchant) == 0 && enchant.canEnchantItem(item);
        double weight = calculateEnchantWeight(item);
        double chance =  power / weight;
        if (chance < Math.random()) return null;

        List<Enchant> available = enchants.stream().filter(filter).collect(Collectors.toList());
        if (available.size() == 0) return null;

        Enchant enchant = available.get((int) (Math.random() * available.size()));
        int level = 1;
        while (level < enchant.getMaxLevel()) {
            double levelWeight = weight + enchant.getMultiplier();
            double levelChance = power / levelWeight;
            if (levelChance > Math.random()) level += 1;
            else break;
        }
        return new Pair<>(enchant, level);
    }
    public void randomEnchantItem(ItemStack item, List<Enchant> enchants, int power) {
        if (item == null || item.getType().equals(Material.AIR)) return;
        do {
            Pair<Enchant, Integer> random = getRandomEnchant(item, enchants, power);
            if (random == null) return;

            setItemEnchant(item, random.getKey(), random.getValue());
        } while (true);
    }
    public void randomEnchantItem(ItemStack item, int power) {
        randomEnchantItem(item, STORAGE.getAll(), power);
    }
    public void randomEnchantItem(ItemStack item, int power, Filter<Enchant> filter) {
        randomEnchantItem(item, filter.apply(STORAGE.getAll()), power);
    }

    @SuppressWarnings("deprecation")
    public abstract static class EnchantFilter extends Filter<Enchant> {
        public static final EnchantFilter IS_CURSED = new EnchantFilter() {
            @Override public boolean test(Enchant enchant) {
                return enchant.isCursed();
            }
        };
        public static final EnchantFilter IS_TREASURE = new EnchantFilter() {
            @Override public boolean test(Enchant enchant) {
                return false;
            }
        };
        public static final EnchantFilter IS_VANILLA = new EnchantFilter() {
            @Override public boolean test(Enchant enchant) {
                return enchant instanceof VanillaEnchant;
            }
        };
        public static final EnchantFilter CAN_BE_TRADED = new EnchantFilter() {
            @Override public boolean test(Enchant enchant) {
                return enchant.canBeTraded();
            }
        };
        public static final EnchantFilter CAN_BE_BARTERED = new EnchantFilter() {
            @Override public boolean test(Enchant enchant) {
                return enchant.canBeBartered();
            }
        };
        public static final EnchantFilter CAN_BE_FISHED = new EnchantFilter() {
            @Override public boolean test(Enchant enchant) {
                return enchant.canBeFished();
            }
        };
    }

    public static class Storage {
        private final List<Enchant> list = new ArrayList<>();

        public Enchant get(NamespacedKey key) {
            for (Enchant enchant: list)
                if (enchant.getKey().toString()
                        .equalsIgnoreCase(key.toString())) return enchant;
            return null;
        }
        public Enchant get(String ID) {
            Enchant result = get(NamespacedKey.fromString(ID));
            if (result != null) return result;
            return get(NamespacedKey.fromString(ID, PLUGIN));
        }
        public Enchant get(Enchantment enchant) {
            return get(enchant.getKey());
        }
        public Enchant get(Object object) {
            return get(String.valueOf(object));
        }

        public List<Enchant> getAll() {
            return new ArrayList<>(list);
        }

        public HashMap<Enchant, Integer> translate(Map<Enchantment, Integer> enchants) {
            HashMap<Enchant, Integer> map = new HashMap<>();
            for (Map.Entry<Enchantment, Integer> entry: enchants.entrySet())
                map.put(get(entry.getKey()), entry.getValue());
            return map;
        }

        public void clear() {
            list.clear();
        }

        public boolean save(Enchant enchant) {
            list.remove(get(enchant.getKey()));
            return list.add(enchant);
        }

        public boolean saveIfAbsent(Enchant enchant) {
            if (get(enchant.getKey()) == null) return save(enchant);
            return false;
        }

        public void sort() {
            list.sort(Comparator.comparing(e -> e.getKey().getKey()));
        }

        public int size() {
            return list.size();
        }
    }
    private static class Loader {
        private static boolean isEnchantEnabled(CustomEnchant enchantment) {
            return enchantment != null && enchantment.canBeRegistered();
        }
        public static CustomEnchant getEnchant(String fileName) {
            List<Class<?>> subs = FileUtils.getClasses(ENCHANTS_FOLDER, fileName, CustomEnchant.class);
            if (subs == null || subs.isEmpty()) return null;

            File JAR = new File(ENCHANTS_FOLDER, fileName);
            CustomEnchant enchantment = createInstance(subs.get(0), JAR);
            try {
                if (isEnchantEnabled(enchantment)) return enchantment;
            } catch (Exception exception) {
                MESSENGER.Warning("Unable to register enchantment called " + enchantment.getName());
                MESSENGER.handleException(exception);
            }
            return null;
        }
        public static List<CustomEnchant> getAllEnchantments() {
            List<CustomEnchant> enchantments = new ArrayList<>();
            for (String filename: ENCHANTS_FOLDER.list()) {
                CustomEnchant enchantment = getEnchant(filename);
                if (enchantment != null) enchantments.add(enchantment);
            }
            return enchantments;
        }
        private static CustomEnchant createInstance(Class<?> clazz, File JAR) {
            if (clazz == null) return null;
            if (!CustomEnchant.class.isAssignableFrom(clazz)) return null;
            Class<? extends CustomEnchant> enchantClass = clazz.asSubclass(CustomEnchant.class);

            try {
                Constructor<?>[] constructors = enchantClass.getConstructors();
                if (constructors.length == 0) {
                    throw new ConstructorNotFound(enchantClass);
                } else {
                    for (Constructor<?> constructor : constructors) {
                        if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(File.class)) {
                            return (CustomEnchant) constructor.newInstance(JAR);
                        }
                    }
                }
            } catch (Throwable ignored) {}
            MESSENGER.Warning("Failed to initialize enchantment from class: " + enchantClass.getName());
            return null;
        }
    }
}