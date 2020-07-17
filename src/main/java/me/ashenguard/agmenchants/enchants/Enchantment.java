package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public abstract class Enchantment {
    protected File configFile;
    protected YamlConfiguration config;

    protected String name;
    protected String description;
    protected int maxLevel;
    protected List<String> applicable;

    public Enchantment(JavaPlugin plugin, String name) {
        configFile = new File(AGMEnchants.getEnchantsFolder(), name + ".yml");
        plugin.saveResource("Enchants/" + name + ".yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);

        this.name = name;
        this.description = config.getString("Description");
        this.applicable = config.getStringList("Applicable");
        this.maxLevel = config.getInt("MaxLevel");
    }

    private boolean is_applicable(Material material) {
        if (applicable.contains(material.name())) return true;
        for (String applicableName: applicable) {
            List<String> applicable = AGMEnchants.config.getStringList("ItemsList." + applicableName);
            if (applicable.contains(material.name())) return true;
        }
        return false;
    }

    public abstract void enchant();
    public abstract boolean load();


    public String getName() {
        return name;
    }
}
