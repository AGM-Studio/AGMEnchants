package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.nbt.NBTCompound;
import me.ashenguard.api.nbt.NBTCompoundList;
import me.ashenguard.api.nbt.NBTItem;
import me.ashenguard.api.nbt.NBTListCompound;
import me.ashenguard.api.utils.FileUtils;
import me.ashenguard.api.utils.extra.Pair;
import me.ashenguard.exceptions.ConstructorNotFound;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;


@SuppressWarnings({"UnusedReturnValue", "unused"})
public class EnchantManager {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private static final Messenger MESSENGER = AGMEnchants.getMessenger();

    private static final String NBT_TAG_NAME = "Enchants";
    private static final String NBT_LVL_NAME = "Level";
    private static final String NBT_ID_NAME = "ID";
    private static final File ENCHANTS_FOLDER = new File(PLUGIN.getDataFolder(), "Enchants");
    static {
        if (!ENCHANTS_FOLDER.exists() && ENCHANTS_FOLDER.mkdirs()) MESSENGER.Debug("General", "Enchants folder wasn't found, A new one created");
    }

    // Storage
    public static class Storage {
        private final List<Enchant> list = new ArrayList<>();

        public Enchant get(String ID) {
            for(Enchant item: list) if (item.ID.equals(ID)) return item;
            return null;
        }
        public Enchant get(Enchantment enchant) {
            return get(enchant.getName());
        }
        public Enchant get(Object object) {
            return get(String.valueOf(object));
        }

        public List<Enchant> getAll() {
            return new ArrayList<>(list);
        }

        public void clear() {
            list.clear();
        }

        public boolean save(Enchant enchant) {
            list.remove(get(enchant.ID));
            return list.add(enchant);
        }

        public boolean saveIfAbsent(Enchant enchant) {
            if (get(enchant.ID) == null) return save(enchant);
            return false;
        }

        public void sort() {
            list.sort(Comparator.comparing(e -> e.ID));
        }

        public int size() {
            return list.size();
        }
    }
    public final Storage STORAGE = new Storage();
    public void loadEnchants() {
        STORAGE.clear();
        for (Enchantment vanillaEnchantment:Enchantment.values()) new VanillaEnchant(vanillaEnchantment).register();
        for (CustomEnchant enchant: getAllEnchantments()) {
            try {
                enchant.register();
            } catch (Throwable throwable) {
                MESSENGER.handleException(throwable);
            }
        }
        STORAGE.sort();

        MESSENGER.Info(String.format("Loaded %d enchantments(Including the vanilla)", STORAGE.size()));
    }

    // NBT Related
    private NBTCompoundList NBTExtractEnchants(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) item = new ItemStack(Material.STONE);
        NBTItem nbt = new NBTItem(item, true);
        return nbt.getCompoundList(NBT_TAG_NAME);
    }
    private NBTListCompound NBTFindEnchant(ItemStack item, Enchant enchant) {
        NBTCompoundList enchantsNBT = NBTExtractEnchants(item);
        for (NBTListCompound entry:enchantsNBT) if (entry.getString(NBT_ID_NAME).equals(enchant.ID)) return entry;
        return null;
    }
    private Pair<Enchant, Integer> NBTTranslateEnchant(NBTCompound compound) {
        if (compound == null) return new Pair<>(null, 0);
        Enchant enchant = STORAGE.get(compound.getString(NBT_ID_NAME));
        int level = compound.getInteger(NBT_LVL_NAME);
        return new Pair<>(enchant, level);
    }

    // Vanilla Extra
    private Map<Enchantment, Integer> extractVanillaEnchants(ItemStack item) {
        Map<Enchantment, Integer> enchants = new HashMap<>(item.getEnchantments());
        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            Map<Enchantment, Integer> storages = ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();
            for (Map.Entry<Enchantment, Integer> enchant: storages.entrySet()) {
                if (enchant.getValue() == 0) continue;
                enchants.computeIfPresent(enchant.getKey(), (e, l) -> Math.max(l, enchant.getValue()));
                enchants.putIfAbsent(enchant.getKey(), enchant.getValue());
            }
        }
        return enchants;
    }
    private void setVanillaEnchant(ItemStack item, Enchant enchant, int level) {
        if (enchant instanceof VanillaEnchant) setVanillaEnchant(item, ((VanillaEnchant) enchant).getEnchantment(), level);
    }
    private void setVanillaEnchant(ItemStack item, Enchantment enchant, int level) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(enchant, level, true);
            item.setItemMeta(meta);
        } else {
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(enchant, level, true);
            item.setItemMeta(meta);
        }
    }

    // Items
    public LinkedHashMap<Enchant, Integer> extractEnchants(ItemStack item) {
        return extractEnchants(item, true);
    }
    public LinkedHashMap<Enchant, Integer> extractEnchants(ItemStack item, boolean vanillaCheck) {
        LinkedHashMap<Enchant, Integer> enchants = new LinkedHashMap<>();

        if (vanillaCheck) {
            Set<Map.Entry<Enchantment, Integer>> vanillaEnchants = extractVanillaEnchants(item).entrySet();
            for (Map.Entry<Enchantment, Integer> entry : vanillaEnchants) {
                Enchant enchant = STORAGE.get(entry.getKey());
                addItemEnchant(item, enchant, entry.getValue());
            }
        }

        NBTCompoundList enchantsNBT = NBTExtractEnchants(item);
        for (NBTListCompound enchant: enchantsNBT) {
            Pair<Enchant, Integer> pair = NBTTranslateEnchant(enchant);
            if (pair.getValue() == 0) continue;
            enchants.put(pair.getKey(), pair.getValue());
        }

        enchants.remove(null);
        return enchants;
    }
    public int getItemEnchant(ItemStack item, Enchant enchant) {
        NBTCompound compound = NBTFindEnchant(item, enchant);
        if (compound == null) return 0;
        return compound.getInteger(NBT_LVL_NAME);
    }
    public int setItemEnchant(ItemStack item, Enchant enchant, int level) {
        if (!enchant.isSafe(level)) return -1;
        NBTCompoundList enchantsNBT = NBTExtractEnchants(item);
        NBTCompound target = NBTFindEnchant(item, enchant);
        if (target == null) {
            NBTCompound compound = enchantsNBT.addCompound();
            compound.setString(NBT_ID_NAME, enchant.ID);
            compound.setInteger(NBT_LVL_NAME, level);
            setVanillaEnchant(item, enchant, level);
            AGMEnchants.getItemManager().applyItemLore(item);
            return level;
        }
        int old = target.getInteger(NBT_LVL_NAME);
        target.setInteger(NBT_LVL_NAME, level);
        setVanillaEnchant(item, enchant, level);
        AGMEnchants.getItemManager().applyItemLore(item);
        return old;
    }
    public boolean delItemEnchant(ItemStack item, Enchant enchant) {
        NBTCompoundList enchantsNBT = NBTExtractEnchants(item);
        boolean result = enchantsNBT.removeIf(compound -> compound.getString(NBT_ID_NAME).equals(enchant.ID));
        AGMEnchants.getItemManager().applyItemLore(item);
        return result;
    }
    public boolean addItemEnchant(ItemStack item, Enchant enchant, int level) {
        if (NBTFindEnchant(item, enchant) != null) return false;
        setItemEnchant(item, enchant, level);
        return true;
    }

    // Loader
    private static boolean isEnchantEnabled(CustomEnchant enchantment) {
        return enchantment != null && enchantment.getName() != null && enchantment.canBeRegistered();
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
