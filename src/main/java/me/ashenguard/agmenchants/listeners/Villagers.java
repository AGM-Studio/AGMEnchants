package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.managers.ItemManager;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.gui.ItemMaker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class Villagers implements Listener {
    private static final ItemManager ITEM_MANAGER = AGMEnchants.getItemManager();

    public Villagers() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());
        AGMEnchants.getMessenger().Debug("General", "Miscellanies has been registered");
    }

    @EventHandler public void VillagerNewTrade(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();
        ITEM_MANAGER.randomize(result, new Random().nextInt(24) + 6, true, false);

        Merchant villager = event.getEntity();
        if (0.05 > new Random().nextDouble()) {
            Rune rune = ITEM_MANAGER.getRandomRune(null);
            MerchantRecipe trade = new MerchantRecipe(rune.getRune(), 0, 1, false, 1, 1);
            ItemStack item = ItemMaker.createSimpleItem(0.2 > new Random().nextDouble() ? "EMERALD" : "EMERALD_BLOCK");
            item.setAmount(new Random().nextInt(64));
            trade.addIngredient(item);
            List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
            recipes.add(trade);
            villager.setRecipes(recipes);
        }
    }
}
