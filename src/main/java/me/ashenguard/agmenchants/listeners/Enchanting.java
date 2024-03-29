package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.utils.Filter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

public class Enchanting extends AdvancedListener {
    private static final Filter<Enchant> FILTER = EnchantManager.EnchantFilter.IS_TREASURE.NOT().AND(EnchantManager.EnchantFilter.IS_VANILLA.NOT());

    @EventHandler
    public void Event(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        item.addEnchantments(event.getEnchantsToAdd());
        EnchantManager.randomEnchantItem(item, event.getExpLevelCost(), FILTER);
    }

    @Override protected void onRegister() {
        plugin.messenger.debug("General", "Enchantment table mechanism has been implemented");
    }
}
