package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.utils.Filter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static me.ashenguard.agmenchants.managers.EnchantManager.EnchantFilter.CAN_BE_BARTERED;

public class Bartering extends AdvancedListener {
    private static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();
    private static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();
    private static final Filter<Enchant> FILTER = CAN_BE_BARTERED;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void runes(PiglinBarterEvent event) {
        if (Math.random() > RUNE_MANAGER.BARTER_CHANCE) return;
        Rune rune = RUNE_MANAGER.getRandomRune();
        event.getOutcome().add(rune.getRune());
    }

    @EventHandler public void enchanting(PiglinBarterEvent event) {
        List<ItemStack> outcome = event.getOutcome();
        for (ItemStack item: outcome) {
            int power = (int) (Math.random() * 40);
            if (power > 20) power -= 10;
            ENCHANT_MANAGER.clearItemEnchants(item);
            ENCHANT_MANAGER.randomEnchantItem(item, power, FILTER);
        }
    }

    @Override protected void onRegister() {
        plugin.messenger.Debug("General", "Bartering mechanism has been registered");
    }
}
