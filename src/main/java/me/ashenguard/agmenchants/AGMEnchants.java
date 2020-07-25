package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.api.Messenger;
import me.ashenguard.agmenchants.api.SpigotUpdater;
import me.ashenguard.agmenchants.api.gui.GUI;
import me.ashenguard.agmenchants.dependencies.papi.PAPI;
import me.ashenguard.agmenchants.enchants.EnchantmentLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class AGMEnchants extends JavaPlugin {
    public static final int pluginID = 8218;
    public static final int resourceID = 81800;
    public static FileConfiguration config;
    public static SpigotUpdater updateChecker;

    private static boolean legacy;
    private static JavaPlugin instance;
    private static File pluginFolder;
    private static File enchantsFolder;

    public static PAPI PAPI;
    public static GUI GUI;



    // ---- Getters ---- //
    public static JavaPlugin getInstance() {
        return instance;
    }
    public static boolean isLegacy() {
        return legacy;
    }
    public static File getPluginFolder() {
        return pluginFolder;
    }
    public static File getEnchantsFolder() {
        return enchantsFolder;
    }

    @Override
    public void onEnable() {
        // ---- Load config ---- //
        Messenger.setup(this.getConfig(), this);
        instance = this;
        loadConfig();

        // ---- Check legacy ---- //
        List<String> versions = Arrays.asList("1.13", "1.14", "1.15", "1.16");
        legacy = true;
        for (String version : versions)
            if (getServer().getVersion().contains(version))
                legacy = false;

        if (isLegacy()) Messenger.Debug("General", "Legacy version detected");

        // ---- Setup data ---- //
        setup();

        // ---- Metrics ---- //
        new Metrics(this, pluginID);
        updateChecker = new SpigotUpdater(this, resourceID);
        if (config.getBoolean("Check.Updates", true) && updateChecker.checkForUpdates()) {
            Messenger.Info("There is a §anew update§r available at §6spigotmc.org§r");
            Messenger.Info("Current version: §c" + getDescription().getVersion() + "§r");
            Messenger.Info("New version: §a" + updateChecker.getLatestVersion() + "§r");
        }
    }

    public static void loadConfig() {
        // ---- Get configuration ---- //
        JavaPlugin plugin = getInstance();

        config = plugin.getConfig();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        Messenger.Info("§5Config§r has been loaded");

        // ---- Set other configs ---- //
        pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists())
            if (pluginFolder.mkdirs())
                Messenger.Debug("General", "Plugin folder wasn't found, A new one created");
        enchantsFolder = new File(pluginFolder, "Enchants");
        if (!enchantsFolder.exists())
            if (enchantsFolder.mkdirs())
                Messenger.Debug("General", "Enchant folder wasn't found, A new one created");
    }

    public static void setup() {
        PAPI = new PAPI();
        GUI = new GUI(isLegacy(), getInstance());

        new Listeners();
        new Commands();

        new EnchantmentLoader(getInstance()).registerAllEnchantments();
    }

    @Override
    public void onDisable() {
        GUI.closeAll();
        Messenger.Info("Plugin disabled");
    }
}
