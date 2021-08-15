package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.LoreManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.utils.SafeCallable;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Grindstone extends AdvancedListener {
    private static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();
    private static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();
    private static final LoreManager ITEM_MANAGER = AGMEnchants.getItemManager();

    private static boolean REMOVE_CURSES;
    private static SafeCallable<Double> ENCHANT_EXP;
    private static LinkedHashMap<Rune.Rarity, Double> RUNE_EXTRACT_CHANCE;
    private static boolean EXTRACT_RUNES;
    private static SafeCallable<Double> REPAIR_BOOST;

    private static Damageable getDamageable(ItemStack item) {
        return item.getItemMeta() instanceof Damageable ? (Damageable) item.getItemMeta() : null;
    }

    @Override protected void onRegister() {
        FileConfiguration config = AGMEnchants.getConfiguration();

        EXTRACT_RUNES = config.getBoolean("Grindstone.ExtractRunes", true);
        REMOVE_CURSES = config.getBoolean("Grindstone.RemoveCurses", false);
        final double base = config.getDouble("Grindstone.Experience.Base", 0.4);
        final double rndExp = config.getDouble("Grindstone.Experience.Randomize", 0.2);
        ENCHANT_EXP = new SafeCallable<>(() -> base + rndExp * Math.random(), 0.5);
        final double rndRepair = config.getDouble("Anvil.Repair.Randomize", 10);
        final double boost = config.getDouble("Anvil.Repair.Boost", 2.5) / 100 + 2;
        REPAIR_BOOST = new SafeCallable<>(() -> boost + rndRepair * Math.random() / 100, 2.025);

        RUNE_EXTRACT_CHANCE = new LinkedHashMap<>();
        for(Rune.Rarity rarity: Rune.Rarity.values())
            RUNE_EXTRACT_CHANCE.put(rarity, config.getDouble(String.format("Features.Grindstone.Enchants.EXP.%s", rarity.getCapitalizedName()), 100.0));

        plugin.messenger.Debug("General", "Grindstone mechanism has been implemented");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void Event(InventoryClickEvent event) {
        InventoryType type = event.getInventory().getType();
        if (!type.name().equals("GRINDSTONE")) return;

        ItemStack item1 = event.getInventory().getItem(0);
        ItemStack item2 = event.getInventory().getItem(1);
        
        if (item1 == null && item2 == null) return;
        if (item1 != null && item2 != null && !item1.getType().equals(item2.getType())) return;
        ItemStack result = item1 != null ? item1.clone() : item2.clone();
        if (result.getType().equals(Material.AIR)) return;
        
        RUNE_MANAGER.delItemRune(result);
        HashMap<Enchant, Integer> enchants = ENCHANT_MANAGER.extractEnchants(result);
        for(Map.Entry<Enchant, Integer> enchant: enchants.entrySet())
            //noinspection deprecation
            if (REMOVE_CURSES || !enchant.getKey().isCursed()) enchant.getKey().removeEnchant(result);

        if (item1 != null && item2 != null) {
            Damageable resultMeta = getDamageable(result);
            Damageable item2Meta = getDamageable(item2);
            if (resultMeta == null || item2Meta == null) return;
            if (resultMeta.getDamage() == 0) return;

            short max = item1.getType().getMaxDurability();
            short durability = (short) (REPAIR_BOOST.call() * max - resultMeta.getDamage() - item2Meta.getDamage());
            resultMeta.setDamage((short) Math.max(0, max - durability));
            result.setItemMeta((ItemMeta) resultMeta);
        }
        
        event.getInventory().setItem(2, ITEM_MANAGER.applyItemLore(result));

        if (event.getSlot() != 2) return;
        HashMap<Enchant, Integer> item1Enchants = ENCHANT_MANAGER.extractEnchants(item1);
        HashMap<Enchant, Integer> item2Enchants = ENCHANT_MANAGER.extractEnchants(item2);

        Player player = (Player) event.getWhoClicked();

        double exp = 0;
        for (Map.Entry<Enchant, Integer> enchant: item1Enchants.entrySet()) exp += enchant.getValue() * enchant.getKey().getMultiplier();
        for (Map.Entry<Enchant, Integer> enchant: item2Enchants.entrySet()) exp += enchant.getValue() * enchant.getKey().getMultiplier();
        exp *= ENCHANT_EXP.call();
        player.giveExp((int) exp);

        if (RUNE_MANAGER.hasItemRune(item1) && EXTRACT_RUNES) {
            Rune rune = RUNE_MANAGER.getItemRune(item1);
            if (Math.random() * 100 < RUNE_EXTRACT_CHANCE.get(rune.getRarity()))
                player.getWorld().dropItem(player.getLocation(), RUNE_MANAGER.getItemRune(item1).getRune());
        }
        if (RUNE_MANAGER.hasItemRune(item2) && EXTRACT_RUNES) {
            Rune rune = RUNE_MANAGER.getItemRune(item2);
            if (Math.random() * 100 < RUNE_EXTRACT_CHANCE.get(rune.getRarity()))
                player.getWorld().dropItem(player.getLocation(), RUNE_MANAGER.getItemRune(item2).getRune());
        }
    }
}
