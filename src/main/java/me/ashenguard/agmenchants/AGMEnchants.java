package me.ashenguard.agmenchants;

import me.ashenguard.agmenchants.managers.MainManager;
import me.ashenguard.api.gui.GUI;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.spigot.SpigotPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

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

    @Override
    public @NotNull List<String> getRequirements() {
        return Collections.singletonList("AGMCore");
    }

    @Override
    public void onPluginEnable() {
        instance = this;

        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists() && pluginFolder.mkdirs()) messenger.Debug("General", "Plugin folder wasn't found, A new one created");
        if (isLegacy()) messenger.Debug("General", "Legacy version detected");

        saveDefaultConfig();
        reloadConfig();

        GUI = new GUI(this);
        manager = new MainManager();
        manager.reload();

        if (PHManager.enable) new Placeholders().register();
    }

    @Override
    public void onPluginDisable() {
        if (GUI != null) GUI.closeAll();
        messenger.Info("Plugin has been disabled");
    }
}
