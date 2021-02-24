package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class Fishing implements Listener {
    public Fishing() {
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());
        AGMEnchants.getMessenger().Debug("General", "Fishing mechanism has been implemented");
    }

    @EventHandler
    public void Event(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH) || !(caught instanceof Item)) return;

        ItemStack item = ((Item) event.getCaught()).getItemStack();
        AGMEnchants.getItemManager().applyItemLore(item);
        AGMEnchants.getItemManager().randomEnchant(item, new Random().nextInt(22) + 8, true, false);
    }
}
