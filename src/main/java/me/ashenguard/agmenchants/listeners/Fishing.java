package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

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
        // if (!itemStack.getType().equals(Material.ENCHANTED_BOOK)) return;
        item.setItemStack(AGMEnchants.GUI.getItemHead(event.getPlayer(), false, "self"));
    }
}
