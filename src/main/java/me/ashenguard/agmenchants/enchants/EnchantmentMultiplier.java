package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class EnchantmentMultiplier {
    private static final File configFile = new File(AGMEnchants.getInstance().getDataFolder(), "VanillaMultipliers.yml");
    static {
        if (!configFile.exists())
            AGMEnchants.getInstance().saveResource("VanillaMultipliers.yml", false);
    }
    private static final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    public final int bookMultiplier;
    public final int itemMultiplier;

    public static EnchantmentMultiplier getMultiplier(@NotNull Enchantment enchantment) {
        return new EnchantmentMultiplier(config.getInt(enchantment.getName() + ".Book"), config.getInt(enchantment.getName() + ".Item"));
    }
    public static EnchantmentMultiplier getMultiplier(@NotNull CustomEnchantment enchantment) {
        return enchantment.multiplier;
    }

    public EnchantmentMultiplier(int bookMultiplier, int itemMultiplier) {
        this.bookMultiplier = bookMultiplier;
        this.itemMultiplier = itemMultiplier;
    }

    public int get(boolean book) {
        return book ? bookMultiplier : itemMultiplier;
    }
}
