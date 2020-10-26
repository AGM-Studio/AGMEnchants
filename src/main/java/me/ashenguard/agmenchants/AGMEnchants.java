package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.commands.CommandCustomEnchantment;
import me.ashenguard.agmenchants.enchants.EnchantmentLoader;
import me.ashenguard.api.SpigotUpdater;
import me.ashenguard.api.gui.GUI;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.placeholderapi.PAPI;
import me.ashenguard.api.utils.VersionUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class AGMEnchants extends JavaPlugin {
    public static final int pluginID = 8218;
    public static final int resourceID = 81800;
    public static SpigotUpdater spigotupdater;
    public static FileConfiguration config;

    public static GUI GUI = null;
    public static PAPI PAPI = null;
    public static Messenger Messenger = null;

    private static boolean legacy;
    private static JavaPlugin instance;
    private static File enchantsFolder;

    // ---- Getters ---- //
    public static JavaPlugin getInstance() {
        return instance;
    }
    public static boolean isLegacy() {
        return legacy;
    }
    public static File getEnchantsFolder() {
        return enchantsFolder;
    }

    @Override
    public void onEnable() {
        // ---- Load config ---- //
        instance = this;
        loadConfig();

        Messenger = new Messenger(this, config);
        Messenger.Info("§5Config§r has been loaded");

        // ---- Development ---- //
        new Metrics(this, pluginID);
        spigotupdater = new SpigotUpdater(this, resourceID);
        Messenger.updateNotification(getServer().getConsoleSender(), spigotupdater);

        // ---- Check legacy ---- //
        legacy = VersionUtils.isLegacy(this);
        if (isLegacy()) Messenger.Debug("General", "Legacy version detected");

        // ---- Setup data ---- //
        setup();
        new CommandCustomEnchantment();
        new Placeholders(this).register();
    }

    public static void loadConfig() {
        // ---- Get configuration ---- //
        JavaPlugin plugin = getInstance();

        config = plugin.getConfig();

        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        // ---- Set other configs ---- //
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) pluginFolder.mkdirs();
        enchantsFolder = new File(pluginFolder, "Enchants");
        if (!enchantsFolder.exists()) enchantsFolder.mkdirs();
    }

    public static void setup() {
        PAPI = new PAPI(getInstance());
        GUI = new GUI(getInstance(), PAPI, isLegacy());

        new Listeners();

        new EnchantmentLoader(getInstance()).registerAllEnchantments();
    }

    @Override
    public void onDisable() {
        if (GUI != null) GUI.closeAll();
        Messenger.Info("Plugin disabled");
    }
}
