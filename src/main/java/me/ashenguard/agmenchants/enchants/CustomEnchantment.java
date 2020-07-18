package me.ashenguard.agmenchants.enchants;

import me.ashenguard.API;
import me.ashenguard.LegacyAPI;
import me.ashenguard.NewAPI;
import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.agmclasses.AGMException;
import me.ashenguard.agmenchants.agmclasses.AGMMessenger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public abstract class CustomEnchantment extends Enchantment {
    protected File configFolder;
    protected File configFile;
    protected YamlConfiguration config;

    protected API api = AGMEnchants.isLegacy() ? new LegacyAPI() : new NewAPI();
    protected BukkitScheduler scheduler = Bukkit.getScheduler();

    protected String name;
    protected String description;

    protected double price;
    protected double exp;
    protected int maxLevel;
    protected List<String> applicable;

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException exception) {
            AGMException.ExceptionHandler(exception, AGMEnchants.getExceptionFolder());
        }
    }

    public CustomEnchantment(int id, String name) {
        super(id);

        configFolder = new File(AGMEnchants.getEnchantsFolder(), "configs");
        if (!configFolder.exists() && configFolder.mkdirs()) AGMMessenger.Debug("General", "Config folder wasn't found, A new one created");;
        configFile = new File(configFolder, name + ".yml");
        if (!configFile.exists()) {
            HashMap<String, Object> defaults = getDefaultConfig();
            for (String path: defaults.keySet())
                config.set(path, defaults.get(path));

            saveConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        this.name = name;
        this.description = config.getString("Description");
        this.applicable = config.getStringList("Applicable");
        this.maxLevel = config.getInt("MaxLevel");
    }

    public boolean is_applicable(Material material) {
        if (material == null) return false;
        if (applicable.contains(material.name())) return true;
        for (String applicableName: applicable) {
            List<String> applicable = AGMEnchants.config.getStringList("ItemsList." + applicableName);
            if (applicable.contains(material.name())) return true;
        }
        return false;
    }

    protected abstract HashMap<String, Object> getDefaultConfig();

    public ItemStack getBook() {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
        itemStack.addEnchantment(this, 1);
        return itemStack;
    }
}
