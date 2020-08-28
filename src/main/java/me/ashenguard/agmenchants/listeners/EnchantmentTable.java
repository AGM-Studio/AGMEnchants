package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantment;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantmentMultiplier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class EnchantmentTable implements Listener {
    public EnchantmentTable() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());

        AGMEnchants.Messenger.Debug("General", "Enchantment table has been implemented");
    }

    @EventHandler
    public void Event(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        int cost = event.getExpLevelCost();
        double power = 0;
        for (int level: event.getEnchantsToAdd().values()) power += level * 2.5;
        item.addEnchantments(event.getEnchantsToAdd());

        List<String> customEnchants = new ArrayList<>(EnchantmentManager.getCustomEnchantments());

        while (true) {
            double chance =  (cost - power) / (double) cost;
            if (chance < new Random().nextDouble()) break;

            List<CustomEnchantment> enchantments = new ArrayList<>();
            for (String enchant: customEnchants) {
                CustomEnchantment enchantment = EnchantmentManager.getCustomEnchantment(enchant);
                if (enchantment.isTreasure() && enchantment.canEnchantItem(item) && enchantment.getEnchantLevel(item) == 0)
                    enchantments.add(enchantment);
            }

            if (enchantments.size() == 0) break;
            CustomEnchantment enchant = enchantments.get(new Random().nextInt(enchantments.size()));
            int level = 1;
            while (level < enchant.getMaxLevel() && (cost - power - level) / (double) cost < new Random().nextDouble()) level += 1;
            EnchantmentManager.addEnchantment(item, enchant, level);
            power += level * CustomEnchantmentMultiplier.getMultiplier(enchant).bookMultiplier;
        }
    }

}
