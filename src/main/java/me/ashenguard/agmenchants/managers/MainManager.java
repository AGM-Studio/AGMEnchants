package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.EnchantCommand;
import me.ashenguard.agmenchants.listeners.*;
import me.ashenguard.agmenchants.runes.RuneCommand;
import me.ashenguard.api.AdvancedListener;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.versions.MCVersion;

public class MainManager {
    private final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private final Messenger MESSENGER = AGMEnchants.getMessenger();

    private LoreManager loreManager;
    private EnchantManager enchantManager;
    private RuneManager runeManager;

    private Configuration groups;

    public LoreManager getLoreManager() {
        return loreManager;
    }
    public EnchantManager getEnchantManager() {
        return enchantManager;
    }
    public RuneManager getRuneManager() {
        return runeManager;
    }

    public void reload() {
        enchantManager = new EnchantManager();
        runeManager = new RuneManager();
        loreManager = new LoreManager();

        loadListeners();
        loadCommands();

        enchantManager.loadEnchants();
        runeManager.loadRunes();

        groups = new Configuration(PLUGIN, "Features/groups.yml");
    }
    
    private void loadCommands() {
        new EnchantCommand();
        new RuneCommand();
    }

    private void loadListeners() {
        AdvancedListener.tryRegister(Anvil.class, PLUGIN);
        AdvancedListener.tryRegister(Enchanting.class, PLUGIN);
        AdvancedListener.tryRegister(Fishing.class, PLUGIN);
        AdvancedListener.tryRegister(Trading.class, PLUGIN);
        AdvancedListener.tryRegister(Miscellanies.class, PLUGIN);
        if (!MCVersion.getMCVersion().isLowerThan(MCVersion.V1_14))
            AdvancedListener.tryRegister(Grindstone.class, PLUGIN);
        if (!MCVersion.getMCVersion().isLowerThan(MCVersion.V1_16))
            AdvancedListener.tryRegister(Bartering.class, PLUGIN);

        MESSENGER.Debug("General", "All listeners has been registered");
    }

    public Configuration getGroups() {
        return groups;
    }
}
