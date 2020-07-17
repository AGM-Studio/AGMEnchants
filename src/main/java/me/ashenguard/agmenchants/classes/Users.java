package me.ashenguard.agmenchants.classes;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.agmclasses.AGMException;
import me.ashenguard.agmenchants.agmclasses.AGMMessenger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Users implements Listener {

    private File configFile;
    private YamlConfiguration config;

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            AGMException.ExceptionHandler(e, AGMEnchants.getExceptionFolder());
        }
    }

    public Users(JavaPlugin plugin) {
        configFile = new File(AGMEnchants.getPluginFolder(), "users.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        saveConfig();

        AGMMessenger.Debug("Users", "§5Users§r has been loaded");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        AGMMessenger.Debug("Users", "Listener has been registered");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        login(player);
    }

    public void login(Player player) {
        config.set("Users." + player.getUniqueId().toString() + ".Name", player.getName());
        AGMMessenger.Debug("Users", "Player logged in", "Player= §6" + player.getName());

        saveConfig();

        AGMMessenger.OPRemind(player);
    }
}
