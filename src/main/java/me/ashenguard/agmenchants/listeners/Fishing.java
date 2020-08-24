package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.CustomEnchantment;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import me.ashenguard.agmenchants.enchants.EnchantmentMultiplier;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class Fishing implements Listener {
    public Fishing() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());

        AGMEnchants.Messenger.Debug("Listeners", "Fishing has been implemented");
    }

    @EventHandler
    public void Event(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH) || !(caught instanceof Item)) return;

        Item item = (Item) caught;
        ItemStack itemStack = item.getItemStack();
        if (!itemStack.getType().equals(Material.ENCHANTED_BOOK) || !(itemStack.getItemMeta() instanceof EnchantmentStorageMeta)) return;
        EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
        Map<Enchantment, Integer> enchantments = itemMeta.getStoredEnchants();

        double power = 0;
        for (int level: enchantments.values()) power += level * 2.5;

        List<String> customEnchants = new ArrayList<>(EnchantmentManager.getCustomEnchantments());

        while (true) {
            double chance =  (30 - power) / (double) 30;
            if (chance < new Random().nextDouble()) break;

            List<CustomEnchantment> customEnchantments = new ArrayList<>();
            for (String enchant: customEnchants) {
                CustomEnchantment enchantment = EnchantmentManager.getCustomEnchantment(enchant);
                if (enchantment.isTreasure() && enchantment.canEnchantItem(itemStack) && enchantment.getEnchantLevel(itemStack) == 0)
                    customEnchantments.add(enchantment);
            }

            if (customEnchantments.size() == 0) break;
            CustomEnchantment enchant = customEnchantments.get(new Random().nextInt(customEnchantments.size()));
            int level = 1;
            while (level < enchant.getMaxLevel() && (30 - power - level) / (double) 30 < new Random().nextDouble()) level += 1;
            EnchantmentManager.addEnchantment(itemStack, enchant, level);
            power += level * EnchantmentMultiplier.getMultiplier(enchant).bookMultiplier;
        }
    }
}
