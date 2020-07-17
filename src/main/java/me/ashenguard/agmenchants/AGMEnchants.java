package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.agmclasses.AGMMessenger;
import me.ashenguard.agmenchants.classes.UpdateChecker;
import me.ashenguard.agmenchants.classes.Users;
import me.ashenguard.agmenchants.classes.Vault;
import me.ashenguard.agmenchants.classes.gui.GUI;
import me.ashenguard.agmenchants.classes.papi.PAPI;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class AGMEnchants extends JavaPlugin {
    public static final int pluginID = 8218;
    public static final int resourceID = 0;
    public static FileConfiguration config;

    private static boolean legacy;
    private static JavaPlugin instance;
    private static File pluginFolder;
    private static File exceptionFolder;
    private static File enchantsFolder;

    private static EnchantmentManager enchantments;
    private static Users users;
    private static Vault vault;
    private static PAPI papi;
    private static GUI gui;



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
    public static File getExceptionFolder() {
        return exceptionFolder;
    }
    public static File getEnchantsFolder() {
        return enchantsFolder;
    }

    public static EnchantmentManager getEnchantmentManager() {
        return enchantments;
    }
    public static Users getUsers() {
        return users;
    }
    public static Vault getVault() {
        return vault;
    }
    public static PAPI getPAPI() {
        return papi;
    }
    public static GUI getGui() {
        return gui;
    }




    @Override
    public void onEnable() {
        // ---- Load config ---- //
        instance = this;
        loadConfig();

        // ---- Check legacy ---- //
        List<String> versions = Arrays.asList("1.13", "1.14", "1.15", "1.16");
        legacy = true;
        for (String version : versions)
            if (getServer().getVersion().contains(version))
                legacy = false;

        if (isLegacy()) AGMMessenger.Debug("General", "Legacy version detected");

        // ---- Vault setup ---- //
        vault = new Vault();
        if (!vault.enable) {
            getServer().getPluginManager().disablePlugin(this);
            AGMMessenger.Warning("§6Vault§r hook failed");
            return;
        }
        AGMMessenger.Info("§6Vault§r hooked");

        // ---- Setup data ---- //
        setup();

        // ---- Metrics ---- //
        new Metrics(this, pluginID);
        new UpdateChecker(this, resourceID).getVersion(version -> {
            if (!getDescription().getVersion().equalsIgnoreCase(version)) {
                AGMMessenger.Info("There is a §anew update§r available at §6spigotmc.org§r");
                AGMMessenger.Info("Current version: §c" + getDescription().getVersion() + "§r");
                AGMMessenger.Info("New version: §a" + version + "§r");
            }
        });

        // ---- Login all players ---- //
        for (Player player: Bukkit.getOnlinePlayers()) {
            users.login(player);
        }
    }

    public static void loadConfig() {
        // ---- Get configuration ---- //
        JavaPlugin plugin = getInstance();

        config = plugin.getConfig();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        AGMMessenger.Info("§5Config§r has been loaded");

        // ---- Set other configs ---- //
        pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists())
            if (pluginFolder.mkdirs())
                AGMMessenger.Debug("General", "Plugin folder wasn't found, A new one created");
        exceptionFolder = new File(pluginFolder, "Exception");
        if (!exceptionFolder.exists())
            if (exceptionFolder.mkdirs())
                AGMMessenger.Debug("General", "Exception folder wasn't found, A new one created");
        enchantsFolder = new File(pluginFolder, "Enchants");
        if (!enchantsFolder.exists())
            if (enchantsFolder.mkdirs())
                AGMMessenger.Debug("General", "Enchant folder wasn't found, A new one created");
    }

    public static void setup() {
        papi = new PAPI();
        users = new Users(getInstance());
        gui = new GUI(isLegacy(), getInstance());
        enchantments = new EnchantmentManager(getInstance());

        enchantments.registerAllEnchantments();
    }

    @Override
    public void onDisable() {
        gui.closeAll();
        AGMMessenger.Info("Plugin disabled");
    }
}
