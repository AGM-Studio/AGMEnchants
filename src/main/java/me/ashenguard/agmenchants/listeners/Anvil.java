package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.LoreManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.nbt.NBTCompound;
import me.ashenguard.api.nbt.NBTCompoundList;
import me.ashenguard.api.nbt.NBTItem;
import me.ashenguard.api.nbt.NBTListCompound;
import me.ashenguard.api.utils.SafeCallable;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

@SuppressWarnings("ConstantConditions")
public class Anvil extends AdvancedListener {
    private static final LoreManager ITEM_MANAGER = AGMEnchants.getItemManager();
    private static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();
    private static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();

    private static boolean PENALTY;
    private static int PENALTY_MAX;
    private static SafeCallable<Double> REPAIR_BOOST;
    private static boolean EXTRACTION;

    private static Damageable getDamageable(ItemStack item) {
        return item.getItemMeta() instanceof Damageable ? (Damageable) item.getItemMeta() : null;
    }

    @Override protected void onRegister() {
        Configuration config = new Configuration(AGMEnchants.getInstance(), "Features/mechanisms.yml");

        PENALTY = config.getBoolean("Anvil.Penalty.Enabled", true);
        PENALTY_MAX = Math.max(0, config.getInt("Anvil.Penalty.Max", 0));
        final double rnd = config.getDouble("Anvil.Repair.Randomize", 10);
        final double bst = config.getDouble("Anvil.Repair.Boost", 2.5) / 100 + 2;
        REPAIR_BOOST = new SafeCallable<>(() -> bst + rnd * Math.random() / 100, 2.025);
        EXTRACTION = config.getBoolean("Anvil.ReverseEnchant", false);

        plugin.messenger.Debug("General", "Anvil mechanism has been implemented");
    }

    private static class PrepareData {
        private int penalty = 0;
        private int cost = 0;
        private boolean ruined = false;
        private boolean enchanting = false;
        private boolean repairing = false;
        private boolean renaming = false;

        private final Map<Enchant, Integer> newEnchants = new HashMap<>();
        private final Map<Enchant, Integer> removedEnchants = new HashMap<>();

        private final ItemStack item;

        PrepareData(ItemStack item) {
            if (PENALTY) {
                penalty = item.getItemMeta() instanceof Repairable ? ((Repairable) item.getItemMeta()).getRepairCost() : 0;
                if (PENALTY_MAX > 0) penalty = Math.min(penalty, PENALTY_MAX);
            }

            this.item = item.clone();
        }

        // Costs: 1 Level - Without Penalty
        void rename(String name, boolean translate) {
            if (name == null || name.equals("")) return;
            if (translate) name = PHManager.translate(name);
            ItemMeta resultMeta = item.getItemMeta();
            resultMeta.setDisplayName(name);
            item.setItemMeta(resultMeta);
            renaming = true;
            cost += 1;
        }

        // Costs: 2 Levels
        public void repair(ItemStack sacrifice) {
            if (sacrifice == null || !item.getType().equals(sacrifice.getType())) return;

            Damageable itemMeta = getDamageable(item);
            Damageable sacrificeMeta = getDamageable(sacrifice);
            if (itemMeta == null || sacrificeMeta == null) return;
            if (itemMeta.getDamage() == 0) return;

            short max = item.getType().getMaxDurability();
            short durability = (short) (REPAIR_BOOST.call() * max - itemMeta.getDamage() - sacrificeMeta.getDamage());
            itemMeta.setDamage((short) Math.max(0, max - durability));
            item.setItemMeta(itemMeta);
            repairing = true;
            cost += 2;
        }

        // Costs: Variable
        public void addEnchants(Map<Enchant, Integer> enchants) {
            for (Map.Entry<Enchant, Integer> entry : enchants.entrySet()) {
                Enchant enchant = entry.getKey();
                if (enchant.canEnchantItem(item)) {
                    int level = entry.getValue();
                    int oldLevel = enchant.getLevel(item);
                    int target = level == oldLevel ? level + 1 : Math.max(level, oldLevel);

                    if (target > oldLevel) {
                        newEnchants.put(enchant, target);
                        if (oldLevel > 0) removedEnchants.put(enchant, oldLevel);
                    }

                    ITEM_MANAGER.secureItemLore(item);
                    ENCHANT_MANAGER.setItemEnchant(item, enchant, target);
                    ITEM_MANAGER.applyItemLore(item);

                    cost += target * enchant.getMultiplier(item);
                    enchanting = true;
                } else {
                    cost += 1;
                }
            }
        }

        public void addRune(Rune rune) {
            if (rune == null || !rune.canRuneItem(item)) return;
            RUNE_MANAGER.setItemRune(item, rune);
            cost += rune.getRarity().cost;
            ruined = true;
        }

        public int getTotalCost() {
            if (enchanting || repairing) return cost + penalty;
            return cost;
        }

        public boolean hasResult() {
            return enchanting || repairing || renaming;
        }

        public ItemStack getResult() {
            if (!hasResult()) return null;
            if ((repairing || enchanting) && item.getItemMeta() instanceof Repairable) {
                ItemMeta resultMeta = item.getItemMeta();
                ((Repairable) resultMeta).setRepairCost((penalty * 2) + 1);
                item.setItemMeta(resultMeta);
            }
            NBTItem nbt = new NBTItem(item, true);
            NBTCompound compound = nbt.addCompound("AnvilStatus");
            NBTCompoundList removed = compound.getCompoundList("Removed");
            for (Map.Entry<Enchant, Integer> entry: removedEnchants.entrySet()) {
                NBTListCompound tempCompound = removed.addCompound();
                tempCompound.setString("id", entry.getKey().getKey().toString());
                tempCompound.setInteger("lvl", entry.getValue());
            }
            NBTCompoundList added = compound.getCompoundList("Added");
            for (Map.Entry<Enchant, Integer> entry: newEnchants.entrySet()) {
                NBTListCompound tempCompound = added.addCompound();
                tempCompound.setString("id", entry.getKey().getKey().toString());
                tempCompound.setInteger("lvl", entry.getValue());
            }
            compound.setBoolean("Ruined", ruined);
            compound.setBoolean("Enchanted", enchanting);
            compound.setBoolean("Repairing", repairing);
            compound.setBoolean("Renaming", renaming);

            return item.clone();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(PrepareAnvilEvent event) {
        ItemStack item = event.getInventory().getItem(0);
        ItemStack sacrifice = event.getInventory().getItem(1);

        if (item == null || item.getType().equals(Material.AIR)) return;

        if (!RUNE_MANAGER.isItemRune(sacrifice))
            if (sacrifice != null && !sacrifice.getType().name().equals("ENCHANTED_BOOK"))
                if (item.getType().equals(sacrifice.getType())) {
                    if (!EXTRACTION) return;
                    if (item.getType().name().equals("ENCHANTED_BOOK")) return;
                }

        PrepareData data = new PrepareData(item);

        boolean translate = event.getViewers().get(0).hasPermission("AGMEnchants.Anvil.Colors");
        data.rename(event.getInventory().getRenameText(), translate);
        data.repair(sacrifice);
        data.addEnchants(ENCHANT_MANAGER.extractEnchants(sacrifice));
        data.addRune(RUNE_MANAGER.getItemRune(sacrifice));

        if (data.hasResult()) {
            event.setResult(AGMEnchants.getItemManager().applyItemLore(data.getResult()));

            getServer().getScheduler().runTask(AGMEnchants.getInstance(), () -> event.getInventory().setRepairCost(data.getTotalCost()));
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
        if (compound == null) return;

        if (compound.hasKey("Enchanted") && compound.getBoolean("Enchanted")) {
            NBTCompoundList removed = compound.getCompoundList("Removed");
            for (NBTCompound temp: removed) {
                Enchant enchant = ENCHANT_MANAGER.STORAGE.get(temp.getString("id"));
                enchant.onDisenchanting(result, temp.getInteger("lvl"));
            }
            NBTCompoundList added = compound.getCompoundList("Added");
            for (NBTCompound temp: added) {
                Enchant enchant = ENCHANT_MANAGER.STORAGE.get(temp.getString("id"));
                enchant.onEnchanting(result, temp.getInteger("lvl"));
            }
        }

        if (compound.hasKey("Ruined") && compound.getBoolean("Ruined")) {
            Rune rune = RUNE_MANAGER.getItemRune(result);
            if (rune != null) rune.onRuneApply(result);
        }
        nbt.removeKey("AnvilStatus");
    }
}
