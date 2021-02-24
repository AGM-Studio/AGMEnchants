package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.enchants.EnchantManager;
import me.ashenguard.agmenchants.managers.ItemManager;
import me.ashenguard.agmenchants.managers.MainManager;
import me.ashenguard.agmenchants.runes.RuneManager;
import me.ashenguard.api.gui.GUI;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.spigot.SpigotPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

@SuppressWarnings("unused")
public final class AGMEnchants extends SpigotPlugin {
    private static AGMEnchants instance;
    public static AGMEnchants getInstance() {
        return instance;
    }

    public static GUI getGUI() {
        return instance.GUI;
    }
    public static MainManager getMainManager() {
        return instance.manager;
    }
    public static ItemManager getItemManager() {
        return instance.manager.getItemManager();
    }
    public static EnchantManager getEnchantManager() {
        return instance.manager.getEnchantManager();
    }
    public static RuneManager getRuneManager() {
        return instance.manager.getRuneManager();
    }
    public static ConfigurationSection getItemsList() {
        return getConfiguration().getConfigurationSection("ItemsList");
    }

    public static FileConfiguration getConfiguration() {
        return instance.getConfig();
    }
    public static Messenger getMessenger() {
        return instance.messenger;
    }

    public GUI GUI = null;
    public MainManager manager = null;

    @Override
    public int getBStatsID() {
        return 8218;
    }

    @Override
    public int getSpigotID() {
        return 81800;
    }

    public void loadPlugin() {
        saveDefaultConfig();
        reloadConfig();

        updateNotification = getConfig().getBoolean("Check.PluginUpdates", true);

        GUI = new GUI(this);
        manager = new MainManager();
        manager.reload();
    }

    @Override
    public void onEnable() {
        instance = this;

        if (getServer().getPluginManager().getPlugin("AGMCore") == null) {
            messenger.Warning("AGMCore is not installed. Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists() && pluginFolder.mkdirs()) messenger.Debug("General", "Plugin folder wasn't found, A new one created");
        if (isLegacy()) messenger.Debug("General", "Legacy version detected");

        loadPlugin();

        if (PHManager.enable) new Placeholders().register();
        messenger.Info("Plugin has been enabled successfully");
    }

    @Override
    public void onDisable() {
        if (GUI != null) GUI.closeAll();
        messenger.Info("Plugin has been disabled");
    }
}
