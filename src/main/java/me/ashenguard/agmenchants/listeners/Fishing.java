package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.utils.Filter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class Fishing extends AdvancedListener {
    private static final Filter<Enchant> FILTER = EnchantManager.EnchantFilter.CAN_BE_FISHED;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void runes(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH) || !(caught instanceof Item)) return;
        if (Math.random() > RuneManager.getFishingChance()) return;

        Item item = (Item) caught;
        Rune rune = RuneManager.getRandomRune();
        item.setItemStack(rune.getRune());
    }

    @EventHandler public void enchanting(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH) || !(caught instanceof Item)) return;

        Filter<Enchant> WorldFilter = new EnchantManager.EnchantFilter() {
            @Override public boolean test(Enchant enchant) {
                return enchant.canBeLooted(event.getPlayer().getWorld());
            }
        };
        ItemStack item = ((Item) caught).getItemStack();
        int power = (int) (Math.random() * 30);
        if (power <= 16) power = power / 2 + 8;
        EnchantManager.clearItemEnchants(item);
        EnchantManager.randomEnchantItem(item, power + 8, FILTER.AND(WorldFilter));
    }

    @Override protected void onRegister() {
        plugin.messenger.Debug("General", "Fishing mechanism has been implemented");
    }
}
