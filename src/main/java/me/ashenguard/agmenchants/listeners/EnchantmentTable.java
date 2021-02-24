package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Bukkit.getServer;

public class EnchantmentTable implements Listener {
    public EnchantmentTable() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());
        AGMEnchants.getMessenger().Debug("General", "Enchantment table mechanism has been implemented");
    }

    @EventHandler
    public void Event(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        item.addEnchantments(event.getEnchantsToAdd());
        AGMEnchants.getItemManager().applyItemLore(item);
        AGMEnchants.getItemManager().randomEnchant(item, event.getExpLevelCost(), false, true);
    }
}
