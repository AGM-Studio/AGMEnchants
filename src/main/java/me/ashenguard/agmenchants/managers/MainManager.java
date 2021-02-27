package me.ashenguard.agmenchants.managers;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.EnchantCommand;
import me.ashenguard.agmenchants.enchants.EnchantManager;
import me.ashenguard.agmenchants.listeners.*;
import me.ashenguard.agmenchants.runes.RuneCommand;
import me.ashenguard.agmenchants.runes.RuneManager;
import me.ashenguard.api.messenger.Messenger;

public class MainManager {
    private final AGMEnchants plugin = AGMEnchants.getInstance();
    private final Messenger messenger = AGMEnchants.getMessenger();

    private final ItemManager itemManager;
    private final EnchantManager enchantManager;
    private final RuneManager runeManager;

    public ItemManager getItemManager() {
        return itemManager;
    }
    public EnchantManager getEnchantManager() {
        return enchantManager;
    }
    public RuneManager getRuneManager() {
        return runeManager;
    }

    public MainManager() {
        enchantManager = new EnchantManager();
        runeManager = new RuneManager();
        itemManager = new ItemManager();
    }

    public void reload() {
        loadListeners();
        loadCommands();

        itemManager.loadConfigs();
        enchantManager.loadEnchants();
        runeManager.loadRunes();
    }
    
    private void loadCommands() {
        new EnchantCommand();
        new RuneCommand();
    }

    private void loadListeners() {
        new Anvil();
        new EnchantmentTable();
        new Grindstone();
        new Fishing();
        new Miscellanies();

        messenger.Debug("General", "All listeners has been registered");
    }
}
