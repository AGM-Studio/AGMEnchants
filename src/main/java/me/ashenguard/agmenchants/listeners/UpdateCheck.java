package me.ashenguard.agmenchants.listeners;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.remote.RemoteEnchant;
import me.ashenguard.agmenchants.remote.RemoteRune;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class UpdateCheck implements Listener {
    public UpdateCheck() {
        if (!AGMEnchants.getConfiguration().getBoolean("Check.Updates")) return;
        getServer().getPluginManager().registerEvents(this, AGMEnchants.getInstance());
        AGMEnchants.getMessenger().Debug("General", "Update check has bee registered");
    }

    @EventHandler
    public void PlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().isOp()) return;
        List<RemoteRune> runes = RemoteRune.fetchAvailableRemoteRunes();
        List<RemoteEnchant> enchants = RemoteEnchant.fetchAvailableRemoteEnchants();

        if (runes.size() + enchants.size() == 0) return;
        AGMEnchants.getMessenger().send(event.getPlayer(), "There are some updates or new enchants or runes available.");
    }
}
