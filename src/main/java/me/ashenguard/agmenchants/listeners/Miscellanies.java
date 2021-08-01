package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.managers.ItemManager;
import me.ashenguard.agmenchants.remote.RemoteEnchant;
import me.ashenguard.agmenchants.remote.RemoteRune;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.agmenchants.runes.RuneManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class Miscellanies implements Listener {
    private static final ItemManager ITEM_MANAGER = AGMEnchants.getItemManager();
    private static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();

    public Miscellanies() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());
        AGMEnchants.getMessenger().Debug("General", "Miscellanies has been registered");
    }

    @EventHandler public void PlayerJoin(PlayerJoinEvent event) {
        if (!AGMEnchants.getConfiguration().getBoolean("Check.Updates") || !event.getPlayer().isOp()) return;
        List<RemoteRune> runes = RemoteRune.fetchAvailableRemoteRunes();
        List<RemoteEnchant> enchants = RemoteEnchant.fetchAvailableRemoteEnchants();

        if (runes.size() + enchants.size() == 0) return;
        AGMEnchants.getMessenger().send(event.getPlayer(), "There are some updates or new enchants or runes available.");
    }

    @EventHandler public void onItemPickup(EntityPickupItemEvent event) {
        ITEM_MANAGER.applyItemLore(event.getItem().getItemStack());
    }
    @EventHandler public void onTreasureGenerate(LootGenerateEvent event) {
        for (ItemStack item: event.getLoot()) ITEM_MANAGER.randomize(item, 16, true, true);
        InventoryHolder holder = event.getInventoryHolder();
        while (holder != null && 0.1 > new Random().nextDouble()) {
            Rune rune = ITEM_MANAGER.getRandomRune(null);
            Inventory inventory = holder.getInventory();
            int slot = new Random().nextInt(inventory.getSize());
            inventory.setItem(slot, rune.getRune());
        }
    }

    @EventHandler public void OnBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (RUNE_MANAGER.hasItemRune(item)) event.setCancelled(true);
    }
}
