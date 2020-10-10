package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.enchants.remote.RemoteEnchantment;
import me.ashenguard.agmenchants.listeners.Anvil;
import me.ashenguard.agmenchants.listeners.EnchantmentTable;
import me.ashenguard.agmenchants.listeners.Fishing;
import me.ashenguard.agmenchants.listeners.Grindstone;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static org.bukkit.Bukkit.getServer;

public class Listeners implements Listener {
    public Listeners() {
        new Anvil();
        new EnchantmentTable();
        new Grindstone();
        new Fishing();

        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());

        AGMEnchants.Messenger.Debug("General", "All listeners has been registered");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        AGMEnchants.Messenger.updateNotification(event.getPlayer(), AGMEnchants.spigotupdater);

        if (AGMEnchants.config.getBoolean("Check.Enchantments") && event.getPlayer().isOp()) {
            int size = RemoteEnchantment.getRemoteEnchantments().size();
            if (size > 0) {
                AGMEnchants.Messenger.reminder(() -> {
                    AGMEnchants.Messenger.send(event.getPlayer(), "There " + (size == 1 ? "is one" : "are " + size) + " enchantments that are not installed or requires update");
                }, 100L);
            }
        }
    }
}
