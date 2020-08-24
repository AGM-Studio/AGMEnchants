package me.ashenguard.agmenchants;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {
    protected JavaPlugin plugin;

    public Placeholders(JavaPlugin plugin){
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

    @NotNull
    @Override
    public String getAuthor(){
        return "Ashenguard";
    }

    @NotNull
    @Override
    public String getIdentifier(){
        return "AGMRanks";
    }

    @NotNull
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) return "";

        try {
            if (identifier.equalsIgnoreCase("Version")) return getVersion();
            if (identifier.equalsIgnoreCase("Authors")) return getAuthor();
        } catch (Exception ignored) {}

        return "";
    }
}
