package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.managers.LoreManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.agmenchants.remote.RemoteEnchant;
import me.ashenguard.agmenchants.remote.RemoteRune;
import me.ashenguard.api.AdvancedListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Miscellanies extends AdvancedListener {
    private static final LoreManager ITEM_MANAGER = AGMEnchants.getItemManager();
    private static final RuneManager RUNE_MANAGER = AGMEnchants.getRuneManager();

    @EventHandler public void PlayerJoin(PlayerJoinEvent event) {
        if (!AGMEnchants.getConfiguration().getBoolean("Check.Updates") || !event.getPlayer().isOp()) return;
        List<RemoteRune> runes = RemoteRune.fetchAvailableRemoteRunes();
        List<RemoteEnchant> enchants = RemoteEnchant.fetchAvailableRemoteEnchants();

        if (runes.size() + enchants.size() == 0) return;
        AGMEnchants.getMessenger().send(event.getPlayer(), "There are some updates or new enchants or runes available.");
    }

    @EventHandler public void OnBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (RUNE_MANAGER.hasItemRune(item)) event.setCancelled(true);
    }

    @Override protected void onRegister() {
        plugin.messenger.Debug("General", "Miscellanies has been registered");
    }
}
