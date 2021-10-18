package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.utils.Filter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static me.ashenguard.agmenchants.managers.EnchantManager.EnchantFilter.IS_VANILLA;

public class WorldGeneration extends AdvancedListener {
    @EventHandler public void runes(LootGenerateEvent event) {
        InventoryHolder holder = event.getInventoryHolder();
        if (holder == null) return;
        Inventory inventory = holder.getInventory();
        while (RuneManager.getLootChance() > Math.random()) {
            int slot = new Random().nextInt(inventory.getSize());
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().equals(Material.AIR)) continue;
            Rune rune = RuneManager.getRandomRune();
            inventory.setItem(slot, rune.getRune());
        }
    }

    @EventHandler public void enchanting(LootGenerateEvent event) {
        Filter<Enchant> WorldFilter = new EnchantManager.EnchantFilter() {
            @Override public boolean test(Enchant enchant) {
                return enchant.canBeLooted(event.getWorld());
            }
        };
        for (ItemStack item: event.getLoot()) {
            int power = (int) (Math.random() * 30);
            if (power <= 16) power = power / 2 + 8;
            EnchantManager.randomEnchantItem(item, power, WorldFilter.AND(IS_VANILLA.NOT()));
        }
    }

    @Override
    protected void onRegister() {
        plugin.messenger.Debug("General", "World generation mechanism has been implemented");
    }
}
