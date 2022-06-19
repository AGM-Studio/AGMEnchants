package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.listeners.*;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.itemstack.ItemLibrary;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PlaceholderManager;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.placeholder.PlaceholderExtension;
import me.ashenguard.api.versions.MCVersion;
import org.jetbrains.annotations.NotNull;

public class MainManager {
    private final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private final Messenger MESSENGER = AGMEnchants.getMessenger();

    private Configuration groups;

    public void reload() {
        loadListeners();
        loadConfigs();

        CommandManager.register();
        LoreManager.loadConfig();
        EnchantManager.loadConfig();
        EnchantManager.loadEnchants();
        RuneManager.loadConfig();
        RuneManager.loadRunes();

        groups = new Configuration(PLUGIN, "Features/groups.yml");

        if (PlaceholderManager.enable) new Placeholders().register();
        ItemLibrary.createLibraryFile(PLUGIN, "agmenchants.yml", "GUI/items.yml");
    }

    private void loadConfigs() {
        new Configuration(AGMEnchants.getInstance(), "GUI/categories.yml", true);
        new Configuration(AGMEnchants.getInstance(), "GUI/list.yml", true);
        new Configuration(AGMEnchants.getInstance(), "GUI/preview.yml", true);
    }

    private void loadListeners() {
        AdvancedListener.tryRegister(Anvil.class, PLUGIN);
        AdvancedListener.tryRegister(Enchanting.class, PLUGIN);
        AdvancedListener.tryRegister(Fishing.class, PLUGIN);
        AdvancedListener.tryRegister(Trading.class, PLUGIN);
        AdvancedListener.tryRegister(Miscellanies.class, PLUGIN);
        AdvancedListener.tryRegister(WorldGeneration.class, PLUGIN);
        if (!MCVersion.getMCVersion().isLowerThan(MCVersion.V1_14))
            AdvancedListener.tryRegister(Grindstone.class, PLUGIN);
        if (!MCVersion.getMCVersion().isLowerThan(MCVersion.V1_16))
            AdvancedListener.tryRegister(Bartering.class, PLUGIN);

        MESSENGER.debug("General", "All listeners has been registered");
    }

    public Configuration getGroups() {
        return groups;
    }

    protected static class Placeholders extends PlaceholderExtension {
        @Override
        public @NotNull String getIdentifier() {
            return "AGMEnchants";
        }

        public Placeholders(){
            super(AGMEnchants.getInstance());

            new Placeholder(this, "total_enchants", ((player, s) -> String.valueOf(EnchantManager.STORAGE.size())));
            new Placeholder(this, "total_runes", ((player, s) -> String.valueOf(RuneManager.STORAGE.size())));
        }
    }
}
