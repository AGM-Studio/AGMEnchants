package me.ashenguard.agmenchants.dependencies.papi;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.api.Messenger;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PAPI {
    public boolean enable = true;

    private PAPIExpansion papiExpansion;

    public PAPI() {
        JavaPlugin agmRanks = AGMEnchants.getInstance();
        if(agmRanks.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiExpansion = new PAPIExpansion(agmRanks);
            papiExpansion.register();
            Messenger.Info("§6PlaceholderAPI§r hooked");
        } else {
            Messenger.Warning("§6PlaceholderAPI§r hook failed");
            enable = false;
        }
    }

    public String translate(OfflinePlayer player, String string) {
        if (enable)
            return PlaceholderAPI.setPlaceholders(player, string);
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public List<String> translate(OfflinePlayer player, List<String> stringList) {
        if (enable)
            return PlaceholderAPI.setPlaceholders(player, stringList);
        List<String> result = new ArrayList<>();
        for (String string:stringList)
            result.add(ChatColor.translateAlternateColorCodes('&', string));
        return result;
    }

    public String translate(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public List<String> translate(List<String> stringList) {
        List<String> result = new ArrayList<>();
        for (String string:stringList)
            result.add(ChatColor.translateAlternateColorCodes('&', string));
        return result;
    }

    // ---- Getters ---- //
    public PAPIExpansion getPAPIExpansion() {
        return papiExpansion;
    }
}
