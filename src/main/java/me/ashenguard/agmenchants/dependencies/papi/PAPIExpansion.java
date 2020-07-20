package me.ashenguard.agmenchants.dependencies.papi;

import me.ashenguard.agmenchants.api.Messenger;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PAPIExpansion extends PlaceholderExpansion {

    private JavaPlugin plugin;

    public PAPIExpansion(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return "Ashenguard";
    }

    @Override
    public String getIdentifier(){
        return "AGMEnchants";
    }

    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        if(player == null) return null;

        Messenger.Debug("PlaceholderAPI", "Try to replace placeholder", "Player= §6" + player.getName(), "Identifier= §6" + identifier);

        if (identifier.equals("Version")) {
            return plugin.getDescription().getVersion();
        }

        if (identifier.equals("Authors")) {
            return "§6" + String.join("§7, §6", plugin.getDescription().getAuthors()) + "§r";
        }

        return null;
    }
}