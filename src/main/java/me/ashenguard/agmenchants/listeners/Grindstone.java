package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantment;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantmentMultiplier;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class Grindstone implements Listener {
    public Grindstone() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());

        AGMEnchants.Messenger.Debug("General", "Grindstone has been implemented");
    }

    @EventHandler
    public void Event(InventoryClickEvent event) {
        InventoryType type = event.getInventory().getType();

        if (!type.name().equals("GRINDSTONE")) return;
        ItemStack result = event.getInventory().getItem(2);

        if (result == null || result.getType().equals(Material.AIR)) return;
        EnchantmentManager.removeAllEnchantments(result);
        event.getInventory().setItem(2, result);

        if (event.getSlot() != 2) return;
        ItemStack item1 = event.getInventory().getItem(0);
        ItemStack item2 = event.getInventory().getItem(1);

        Map<CustomEnchantment, Integer> customEnchants1 = EnchantmentManager.extractEnchantments(item1);
        Map<CustomEnchantment, Integer> customEnchants2 = EnchantmentManager.extractEnchantments(item2);
        Map<Enchantment, Integer> enchants1 = item1.getEnchantments();
        Map<Enchantment, Integer> enchants2 = item1.getEnchantments();

        Player player = (Player) event.getWhoClicked();

        double exp = 0;
        for (Map.Entry<CustomEnchantment, Integer> enchant: customEnchants1.entrySet()) exp += enchant.getValue() * enchant.getKey().getMultiplier().bookMultiplier;
        for (Map.Entry<CustomEnchantment, Integer> enchant: customEnchants2.entrySet()) exp += enchant.getValue() * enchant.getKey().getMultiplier().bookMultiplier;
        for (Map.Entry<Enchantment, Integer> enchant: enchants1.entrySet()) exp += enchant.getValue() * CustomEnchantmentMultiplier.getMultiplier(enchant.getKey()).bookMultiplier;
        for (Map.Entry<Enchantment, Integer> enchant: enchants2.entrySet()) exp += enchant.getValue() * CustomEnchantmentMultiplier.getMultiplier(enchant.getKey()).bookMultiplier;
        exp *= new Random().nextDouble() * 0.2 + 0.4;
        player.giveExp((int) exp);
    }
}
