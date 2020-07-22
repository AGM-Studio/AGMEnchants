package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.api.Messenger;
import me.ashenguard.agmenchants.enchants.CustomEnchantment;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class Listeners implements Listener {
    public Listeners() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());

        Messenger.Debug("Listeners", "Listeners has been registered");
    }

    @EventHandler
    public void onAnvilUsage(PrepareAnvilEvent event) {
        ItemStack item = event.getInventory().getItem(0);
        ItemStack added = event.getInventory().getItem(1);
        ItemStack result = event.getResult();

        if (added == null || item == null) return;
        if (!(added.getType().equals(Material.ENCHANTED_BOOK) || added.getType().equals(item.getType()))) return;

        // ---- Add Custom Enchantments ---- //
        HashMap<CustomEnchantment, Integer> oldEnchants = EnchantmentManager.extractEnchantments(item);
        HashMap<CustomEnchantment, Integer> newEnchants = EnchantmentManager.extractEnchantments(added);
        int cost = 0;

        for (Map.Entry<CustomEnchantment, Integer> enchant: newEnchants.entrySet()) {
            if (!enchant.getKey().canEnchantItem(item)) continue;
            int oldLevel = oldEnchants.getOrDefault(enchant.getKey(), 0);
            if (enchant.getValue() > oldLevel) {
                oldEnchants.put(enchant.getKey(), enchant.getValue());
                cost += Math.pow(2, enchant.getValue()) - 1;
                Messenger.Debug("Enchants", "Trying to add enchantment to item", "Enchantment= §6" + enchant.getKey().getName(), "Item= §6" + item.getType().name());
            } else if (enchant.getValue() == oldLevel && enchant.getKey().getMaxLevel() > enchant.getValue()) {
                oldEnchants.put(enchant.getKey(), enchant.getValue() + 1);
                cost += Math.pow(2, enchant.getValue() + 1) - 1;
                Messenger.Debug("Enchants", "Trying to add enchantment to item", "Enchantment= §6" + enchant.getKey().getName(), "Item= §6" + item.getType().name());
            }
        }

        if (cost > 0) {
            if (result == null || result.getType().equals(Material.AIR)) result = item.clone();

            result = EnchantmentManager.clearEnchantments(result);
            result = EnchantmentManager.addEnchantments(result, oldEnchants);

            event.setResult(result);

            int finalCost = cost;
            getServer().getScheduler().runTask(AGMEnchants.getInstance(), () -> event.getInventory().setRepairCost(finalCost));
        } else if (result != null && !result.getType().equals(Material.AIR)) {
            EnchantmentManager.glow(result);
            event.setResult(result);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Messenger.OPRemind(event.getPlayer());
    }
}
