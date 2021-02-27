package me.ashenguard.agmenchants.runes;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.nbt.NBTItem;
import me.ashenguard.api.utils.FileUtils;
import me.ashenguard.exceptions.ConstructorNotFound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public class RuneManager {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private static final Messenger MESSENGER = AGMEnchants.getMessenger();

    private static final String NBT_TAG_NAME = "Rune";
    private static final String NBT_ORIGINAL = "OriginalRune";
    private static final File RUNES_FOLDER = new File(PLUGIN.getDataFolder(), "Runes");
    static {
        if (!RUNES_FOLDER.exists() && RUNES_FOLDER.mkdirs()) MESSENGER.Debug("General", "Rune folder wasn't found, A new one created");
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
    }
    public final Storage STORAGE = new RuneManager.Storage();
    public void loadRunes() {
        STORAGE.clear();
        for (Rune rune: getAllRunes()) {
            try {
                rune.register();
            } catch (Throwable throwable) {
                MESSENGER.handleException(throwable);
            }
        }
        STORAGE.sort();

        MESSENGER.Info(String.format("Loaded %d Runes", STORAGE.size()));
    }

    // NBT Related
    private String NBTExtractRune(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return null;
        NBTItem nbt = new NBTItem(item);
        if (nbt.hasKey(NBT_TAG_NAME)) return nbt.getString(NBT_TAG_NAME);
        return null;
    }
    private boolean NBTSetRune(ItemStack item, Rune rune) {
        return NBTSetRuneOriginal(item, rune, false);
    }
    private boolean NBTSetRuneOriginal(ItemStack item, Rune rune, boolean orig) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        AGMEnchants.getItemManager().secureItemLore(item);

        NBTItem nbt = new NBTItem(item, true);
        if (rune == null) nbt.removeKey(NBT_TAG_NAME);
        else nbt.setString(NBT_TAG_NAME, rune.ID);
        if (rune != null) nbt.setBoolean(NBT_ORIGINAL, orig);
        AGMEnchants.getItemManager().applyItemLore(item);
        return true;
    }
    private boolean NBTIsOriginalRune(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        NBTItem nbt = new NBTItem(item, true);
        return nbt.hasKey(NBT_ORIGINAL) && nbt.getBoolean(NBT_ORIGINAL);
    }

    // Items
    public Rune getItemRune(ItemStack item) {
        String ID = NBTExtractRune(item);
        return STORAGE.get(ID);
    }
    public boolean setItemRune(ItemStack item, Rune rune, boolean orig) {
        return NBTSetRuneOriginal(item, rune, orig);
    }
    public boolean setItemRune(ItemStack item, Rune rune) {
        return NBTSetRune(item, rune);
    }
    public boolean delItemRune(ItemStack item) {
        return NBTSetRune(item, null);
    }
    public boolean hasItemRune(ItemStack item) {
        return getItemRune(item) != null;
    }
    public boolean isItemRune(ItemStack item) {
        return NBTIsOriginalRune(item);
    }
    
    // Loader
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
        Class<? extends Rune> enchantClass = clazz.asSubclass(Rune.class);

        try {
            Constructor<?>[] constructors = enchantClass.getConstructors();
            if (constructors.length == 0) {
                throw new ConstructorNotFound(enchantClass);
            } else {
                for (Constructor<?> constructor : constructors) {
                    if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(File.class)) {
                        return (Rune) constructor.newInstance(JAR);
                    }
                }
            }
        } catch (Throwable ignored) {}
        MESSENGER.Warning("Failed to initialize rune from class: " + enchantClass.getName());
        return null;
    }
}
