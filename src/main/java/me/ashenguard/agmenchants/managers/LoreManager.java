package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.nbt.NBTItem;
import me.ashenguard.api.nbt.NBTList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;


@SuppressWarnings({"UnusedReturnValue", "unused"})
public class LoreManager {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();

    private static final String NBT_SECURE_LORE = "SecureLore";
    private static final String NBT_SECURE_DONE = "LoreSecured";

    private final Configuration config;

    private String SEPARATOR_LINE = "§f------------------------------";
    private boolean ABOVE_LORE = true;
    private boolean SHOW_LORE = false;
    private boolean COMPRESSIBLE = false;
    private int COMPRESS_LIMIT = 8;
    private int LINE_LIMIT = 50;
    private String SPACING = "    ";

    private static boolean isRawItem(ItemStack item) {
        return item.getEnchantments().size() == 0 && !AGMEnchants.getRuneManager().hasItemRune(item);
    }

    public LoreManager() {
        config = new Configuration(PLUGIN, "Features/lore.yml");

        ABOVE_LORE = config.getBoolean("AboveItemLore", ABOVE_LORE);
        SHOW_LORE = config.getBoolean("ShowLore", SHOW_LORE);
        SEPARATOR_LINE = config.getString("SeparatorLine", SEPARATOR_LINE);
        COMPRESSIBLE = config.getBoolean("Compress", COMPRESSIBLE);
        COMPRESS_LIMIT = config.getInt("CompressLimit", COMPRESS_LIMIT);
        LINE_LIMIT = config.getInt("LineLimit", LINE_LIMIT);
        SPACING = config.getString("Spacing", SPACING);

        if (config.getBoolean("UpdateOnItemPickUp", false)) {
            getServer().getPluginManager().registerEvents(new Updater(this), PLUGIN);
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public void secureItemLore(ItemStack item) {
        if (isRawItem(item)) setSecureItemLore(item, getItemLore(item));
        else setSecureItemLore(item, new ArrayList<>());
    }
    public void setSecureItemLore(ItemStack item, List<String> lore) {
        if (item == null || item.getType().equals(Material.AIR)) return;
        NBTItem nbt = new NBTItem(item, true);

        if (nbt.hasKey(NBT_SECURE_DONE) && nbt.getBoolean(NBT_SECURE_DONE)) return;
        NBTList<String> list = nbt.getStringList(NBT_SECURE_LORE);
        nbt.setBoolean(NBT_SECURE_DONE, true);
        list.clear();
        list.addAll(lore);
    }
    public List<String> getSecureLore(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return null;
        NBTItem nbt = new NBTItem(item);
        return nbt.hasKey(NBT_SECURE_LORE) ? new ArrayList<>(nbt.getStringList(NBT_SECURE_LORE)) : nbt.hasKey(NBT_SECURE_DONE) && nbt.getBoolean(NBT_SECURE_DONE) ? new ArrayList<>() : null;
    }
    public List<String> getItemLore(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta() == null) return new ArrayList<>();
        List<String> secure = getSecureLore(item);
        if (secure != null) return secure;
        if (!isRawItem(item)) return new ArrayList<>();
        ItemMeta meta = item.getItemMeta();
        return meta.hasLore() ? meta.getLore() : new ArrayList<>();
    }
    public ItemStack setItemDisplay(ItemStack item, String name, List<String> lore, Iterable<ItemFlag> flags) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (lore != null) meta.setLore(trimList(lore));
        if (name != null) meta.setDisplayName(name);
        if (flags != null) flags.forEach(meta::addItemFlags);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack applyItemLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;

        final List<String> oldLore = getItemLore(item);
        final List<String> newLore = new ArrayList<>();

        newLore.addAll(getRuinsLore(item));
        newLore.addAll(getEnchantsLore(item));
        if (oldLore.size() > 0) {
            newLore.add(ABOVE_LORE ? newLore.size() : 0, SEPARATOR_LINE);
            newLore.addAll(ABOVE_LORE ? newLore.size() : 0, oldLore);
        }
        return setItemDisplay(item, null, newLore, Arrays.asList(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS));
    }

    private List<String> trimList(List<String> list) {
        List<String> stringList = new ArrayList<>();
        for (String line: list) stringList.addAll(breakString(line));
        while (!stringList.isEmpty() && stringList.get(0).isEmpty()) stringList.remove(0);
        while (!stringList.isEmpty() && stringList.get(stringList.size() - 1).isEmpty()) stringList.remove(stringList.size() - 1);
        return stringList;
    }
    private List<String> breakString(String string) {
        List<String> list = new ArrayList<>();

        Pattern pattern = Pattern.compile("(§.)+");
        while (true){
            int index = string.indexOf(' ');
            while (index != -1 && ChatColor.stripColor(string.substring(0, index)).length() < LINE_LIMIT)
                index = string.indexOf(' ', index + 1);
            index = index == -1 ? string.length() : index;
            String line = string.substring(0, index);
            list.add(line);
            string = string.substring(line.length());
            if (string.isEmpty()) break;
            String color = "";
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) color = matcher.group();
            string = color + string;
        }
        return list;
    }
    private List<String> getEnchantsLore(ItemStack item) {
        List<String> lore = new ArrayList<>();
        HashMap<Enchant, Integer> enchants = AGMEnchants.getEnchantManager().extractEnchants(item);

        if (COMPRESSIBLE && enchants.size() > COMPRESS_LIMIT) {
            lore.add(enchants.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getKey().getKey())).map(enchant -> enchant.getKey().getColoredName(enchant.getValue())).collect(Collectors.joining("§r, ")));
        } else {
            for (Map.Entry<Enchant, Integer> enchant : enchants.entrySet()) {
                lore.add(enchant.getKey().getColoredName(enchant.getValue()));
                if (SHOW_LORE) lore.add(SPACING + PHManager.translate(enchant.getKey().getLore(enchant.getValue())));
            }
        }
        return lore;
    }
    private List<String> getRuinsLore(ItemStack item) {
        final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();
        List<String> lore = new ArrayList<>();
        Rune rune = RUNE_MANAGER.getItemRune(item);
        if (rune != null) {
            lore.add(rune.getColoredName());
            if (SHOW_LORE) lore.add(SPACING + PHManager.translate(rune.getLore()));
        }

        return lore;
    }

    private static class Updater implements Listener {
        private final LoreManager manager;

        private Updater(LoreManager manager) {
            this.manager = manager;
        }

        @EventHandler public void onItemPickup(EntityPickupItemEvent event) {
            manager.applyItemLore(event.getItem().getItemStack());
        }
    }
}
