package me.ashenguard.agmenchants;

import me.ashenguard.agmcore.Placeholders;
import me.ashenguard.agmenchants.managers.MainManager;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.placeholder.Translations;
import me.ashenguard.api.spigot.SpigotPlugin;
import me.ashenguard.api.versions.MCVersion;
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

    public static Translations getTranslations() {
        return instance.translation;
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
        if (MCVersion.getMCVersion().isLowerThan(MCVersion.V1_17)) messenger.warning("This plugin is only supported on 1.17+, It might not work well on " + MCVersion.getMCVersion().version.toString(true));

        instance = this;

        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists() && pluginFolder.mkdirs()) messenger.debug("General", "Plugin folder wasn't found, A new one created");

        saveDefaultConfig();
        reloadConfig();

        manager = new MainManager();
        manager.reload();

        if (PHManager.enable) new Placeholders().register();
    }
}
