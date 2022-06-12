package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.Rune;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.gui.ItemMaker;
import me.ashenguard.api.utils.Filter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Trading extends AdvancedListener {
    private static final Filter<Enchant> FILTER = EnchantManager.EnchantFilter.CAN_BE_TRADED;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void runes(VillagerAcquireTradeEvent event) {
        Merchant villager = event.getEntity();
        if (RuneManager.getTradeChance() < Math.random()) return;

        Rune rune = RuneManager.getRandomRune();
        ItemStack price = ItemMaker.createSimpleItem(1 - rune.getRarity().getChance() > new Random().nextDouble() ? "EMERALD" : "EMERALD_BLOCK");
        price.setAmount((int) (Math.random() * (32 * rune.getRarity().getChance())) + 32);

        MerchantRecipe trade = new MerchantRecipe(rune.getRune(), 0, 1, false, rune.getRarity().getCost(), rune.getRarity().getCost());
        trade.addIngredient(price);
        List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
        recipes.add(trade);
        villager.setRecipes(recipes);
    }

    @EventHandler public void enchanting(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();
        int power = (int) (Math.random() * 40);
        if (power > 20) power -= 10;
        EnchantManager.clearItemEnchants(result);
        EnchantManager.randomEnchantItem(result, power, FILTER);
    }

    @Override protected void onRegister() {
        plugin.messenger.debug("General", "Village Trading has been registered");
    }
}
