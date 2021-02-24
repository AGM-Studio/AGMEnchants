package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.EnchantManager;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.agmenchants.runes.RuneManager;
import me.ashenguard.api.nbt.NBTCompound;
import me.ashenguard.api.nbt.NBTItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class Anvil implements Listener {
    private static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();
    private static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();

    public Anvil() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());
        AGMEnchants.getMessenger().Debug("General", "Anvil mechanism has been implemented");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(PrepareAnvilEvent event) {
        ItemStack item = event.getInventory().getItem(0);
        ItemStack sacrifice = event.getInventory().getItem(1);

        if (item == null || item.getType().equals(Material.AIR)) return;
        if (!RUNE_MANAGER.isItemRune(sacrifice))
            if (sacrifice == null || !(sacrifice.getType().equals(Material.ENCHANTED_BOOK) || item.getType().equals(sacrifice.getType()))) return;

        // ---- Prior Penalty ---- //
        int initCost = 0;
        if (item.getItemMeta() instanceof Repairable) initCost = ((Repairable) item.getItemMeta()).getRepairCost();
        int repairCost = initCost;

        // ---- Create Result ---- //
        boolean enchanting = false;
        boolean repairing = false;
        boolean naming = false;
        ItemStack result = item.clone();

        // ---- Naming and Repairing ---- //
        String name = event.getInventory().getRenameText();
        if (name != null && !name.equals("")) {
            ItemMeta resultMeta = result.getItemMeta();
            resultMeta.setDisplayName(name);
            result.setItemMeta(resultMeta);
            repairCost += 1;
            naming = true;
        }
        if (item.getType().equals(sacrifice.getType())) {
            if (result.getDurability() < result.getType().getMaxDurability()) {
                short durability = (short) (item.getDurability() + sacrifice.getDurability() + Math.floor(item.getType().getMaxDurability() / 20.0));
                result.setDurability((short) Math.max(item.getType().getMaxDurability(), durability));
                repairCost += 2;
                repairing = true;
            }
        }

        // ---- Apply Enchants ---- //
        LinkedHashMap<Enchant, Integer> sacrificeEnchants = ENCHANT_MANAGER.extractEnchants(sacrifice);
        for (Map.Entry<Enchant, Integer> entry : sacrificeEnchants.entrySet()) {
            Enchant enchant = entry.getKey();
            if (enchant.canEnchantItem(result)) {
                int level = entry.getValue();
                int oldLevel = enchant.getLevel(item);
                int target = level == oldLevel ? level + 1 : Math.max(level, oldLevel);

                enchant.applyEnchant(result, target);
                repairCost += target * enchant.getMultiplier(result);
                enchanting = true;
            } else {
                repairCost += 1;
            }
        }

        // ---- Apply Rune ---- //
        Rune sacrificeRune = RUNE_MANAGER.getItemRune(sacrifice);
        if (sacrificeRune != null && sacrificeRune.canRuneItem(item)) {
            RUNE_MANAGER.setItemRune(result, sacrificeRune);
            repairCost += sacrificeRune.getRarity().cost;
            enchanting = true;
        }

        // ---- Set result ---- //
        if (enchanting || repairing || naming) {
            if (enchanting || repairing) {
                if (result.getItemMeta() instanceof Repairable) {
                    ItemMeta resultMeta = result.getItemMeta();
                    ((Repairable) resultMeta).setRepairCost((initCost * 2) + 1);
                    result.setItemMeta(resultMeta);
                }
            }
            NBTItem nbt = new NBTItem(result, true);
            NBTCompound compound = nbt.addCompound("AnvilStatus");
            compound.setBoolean("Enchanted", enchanting);
            compound.setBoolean("Repairing", repairing);
            compound.setBoolean("Renaming", naming);
            event.setResult(AGMEnchants.getItemManager().applyItemLore(result));

            final int finalCost = repairCost;
            getServer().getScheduler().runTask(AGMEnchants.getInstance(), () -> event.getInventory().setRepairCost(finalCost));
        } else {
            getServer().getScheduler().runTask(AGMEnchants.getInstance(), () -> event.setResult(new ItemStack(Material.AIR)));
            getServer().getScheduler().runTask(AGMEnchants.getInstance(), () -> event.getInventory().setItem(2, new ItemStack(Material.AIR)));
            getServer().getScheduler().runTask(AGMEnchants.getInstance(), () -> event.getInventory().setRepairCost(0));
        }
    }

    @EventHandler
    public void onEnchanting(InventoryClickEvent event) {
        InventoryType type = event.getInventory().getType();

        if (!type.name().equals("ANVIL")) return;
        ItemStack result = event.getInventory().getItem(2);
        
        if (result == null || result.getType().equals(Material.AIR)) return;
        NBTItem nbt = new NBTItem(result, true);
        NBTCompound compound = nbt.getCompound("AnvilStatus");
        if (compound == null || !compound.getBoolean("Enchanted")) return;
        LinkedHashMap<Enchant, Integer> enchants = ENCHANT_MANAGER.extractEnchants(result);
        for(Map.Entry<Enchant, Integer> enchant: enchants.entrySet()) enchant.getKey().onEnchanting(result, enchant.getValue());
        Rune rune = RUNE_MANAGER.getItemRune(result);
        if (rune != null) rune.onRuneApply(result);
        nbt.removeKey("AnvilStatus");
    }
}
