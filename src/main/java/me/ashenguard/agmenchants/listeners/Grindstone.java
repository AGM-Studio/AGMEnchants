package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.Rune;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.utils.SafeCallable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Grindstone extends AdvancedListener {
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

        plugin.messenger.debug("General", "Grindstone mechanism has been implemented");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void UseEvent(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!inventory.getType().name().equals("GRINDSTONE") || !event.getSlotType().equals(InventoryType.SlotType.RESULT)) return;

        ItemStack item1 = inventory.getItem(0);
        ItemStack item2 = inventory.getItem(1);

        HashMap<Enchant, Integer> item1Enchants = EnchantManager.extractEnchants(item1);
        HashMap<Enchant, Integer> item2Enchants = EnchantManager.extractEnchants(item2);

        Player player = (Player) event.getWhoClicked();

        double exp = 0;
        for (Map.Entry<Enchant, Integer> enchant: item1Enchants.entrySet()) exp += enchant.getValue() * enchant.getKey().getMultiplier();
        for (Map.Entry<Enchant, Integer> enchant: item2Enchants.entrySet()) exp += enchant.getValue() * enchant.getKey().getMultiplier();
        exp *= ENCHANT_EXP.call();
        player.giveExp((int) exp);

        if (item1 != null && RuneManager.hasItemRune(item1) && EXTRACT_RUNES) {
            Rune rune = RuneManager.getItemRune(item1);
            if (rune != null && Math.random() * 100 < RUNE_EXTRACT_CHANCE.get(rune.getRarity()))
                player.getWorld().dropItem(player.getLocation(), rune.getRune());
        }
        if (item2 != null && RuneManager.hasItemRune(item2) && EXTRACT_RUNES) {
            Rune rune = RuneManager.getItemRune(item2);
            if (rune != null && Math.random() * 100 < RUNE_EXTRACT_CHANCE.get(rune.getRarity()))
                player.getWorld().dropItem(player.getLocation(), rune.getRune());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void Event(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!inventory.getType().name().equals("GRINDSTONE")) return;

        Bukkit.getScheduler().runTask(AGMEnchants.getInstance(), new UpdateInventory(event.getInventory()));
    }

    private static class UpdateInventory implements Runnable {
        private final Inventory inventory;

        public UpdateInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public void run() {
            ItemStack item1 = inventory.getItem(0);
            ItemStack item2 = inventory.getItem(1);

            inventory.setItem(2, new PrepareData(item1, item2).getResult());
        }
    }

    private static class PrepareData {
        private final ItemStack result, item1, item2;

        boolean rune_removed = false, enchant_removed = false, repaired = false;

        public PrepareData(ItemStack item1, ItemStack item2) {
            if (item1 != null) {
                this.item1 = item1;
                this.item2 = item2;
            } else {
                this.item1 = item2;
                this.item2 = null;
            }

            this.result = item1 != null && (item2 == null || item2.getType() == item1.getType())? item1.clone() : null;
        }

        public ItemStack getResult() {
            removeRune();
            disenchant();
            repair();
            return rune_removed || enchant_removed || repaired ? result : null;
        }

        private void removeRune() {
            if (result == null || !RuneManager.hasItemRune(result)) return;
            RuneManager.delItemRune(result);
            rune_removed = true;
        }

        @SuppressWarnings("deprecation")
        private void disenchant() {
            if (result == null) return;
            HashMap<Enchant, Integer> enchants = EnchantManager.extractEnchants(result);
            for(Map.Entry<Enchant, Integer> enchant: enchants.entrySet())
                if (REMOVE_CURSES || !enchant.getKey().isCursed()) {
                    enchant_removed = true;
                    enchant.getKey().removeEnchant(result);
                }
        }

        private void repair() {
            if (result == null || item2 == null) return;
            Damageable resultMeta = getDamageable(result);
            Damageable item2Meta = getDamageable(item2);
            if (resultMeta != null && item2Meta != null && resultMeta.getDamage() > 0) {
                short max = item1.getType().getMaxDurability();
                short durability = (short) (REPAIR_BOOST.call() * max - resultMeta.getDamage() - item2Meta.getDamage());
                resultMeta.setDamage((short) Math.max(0, max - durability));
                result.setItemMeta(resultMeta);
                repaired = true;
            }
        }
    }
}
