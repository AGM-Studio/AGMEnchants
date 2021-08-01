package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.EnchantManager;
import me.ashenguard.agmenchants.managers.ItemManager;
import me.ashenguard.agmenchants.runes.RuneManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class Grindstone implements Listener {
    private static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();
    private static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();
    private static final ItemManager ITEM_MANAGER = AGMEnchants.getItemManager();

    public Grindstone() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());
        AGMEnchants.getMessenger().Debug("General", "Grindstone mechanism has been implemented");
    }

    @EventHandler
    public void Event(InventoryClickEvent event) {
        InventoryType type = event.getInventory().getType();

        if (!type.name().equals("GRINDSTONE")) return;
        ItemStack result = event.getInventory().getItem(2);

        if (result == null || result.getType().equals(Material.AIR)) return;
        RUNE_MANAGER.delItemRune(result);
        LinkedHashMap<Enchant, Integer> enchants = ENCHANT_MANAGER.extractEnchants(result);
        for(Map.Entry<Enchant, Integer> enchant: enchants.entrySet()) enchant.getKey().removeEnchant(result);
        event.getInventory().setItem(2, ITEM_MANAGER.applyItemLore(result));

        if (event.getSlot() != 2) return;
        ItemStack item1 = event.getInventory().getItem(0);
        ItemStack item2 = event.getInventory().getItem(1);

        LinkedHashMap<Enchant, Integer> item1Enchants = ENCHANT_MANAGER.extractEnchants(item1);
        LinkedHashMap<Enchant, Integer> item2Enchants = ENCHANT_MANAGER.extractEnchants(item2);

        Player player = (Player) event.getWhoClicked();

        double exp = 0;
        for (Map.Entry<Enchant, Integer> enchant: item1Enchants.entrySet()) exp += enchant.getValue() * enchant.getKey().getMultiplier();
        for (Map.Entry<Enchant, Integer> enchant: item2Enchants.entrySet()) exp += enchant.getValue() * enchant.getKey().getMultiplier();
        exp *= new Random().nextDouble() * 0.2 + 0.4;
        player.giveExp((int) exp);

        if (RUNE_MANAGER.hasItemRune(item1)) player.getWorld().dropItem(player.getLocation(), RUNE_MANAGER.getItemRune(item1).getRune());
        if (RUNE_MANAGER.hasItemRune(item2)) player.getWorld().dropItem(player.getLocation(), RUNE_MANAGER.getItemRune(item2).getRune());
    }
}
