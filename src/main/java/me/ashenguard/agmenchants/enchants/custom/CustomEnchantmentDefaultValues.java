package me.ashenguard.agmenchants.enchants.custom;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class CustomEnchantmentDefaultValues {
    /* Configurable */
    public abstract String name();
    public abstract String description();
    public abstract List<String> applicable();
    public abstract boolean treasure();
    public abstract boolean cursed();
    public abstract CustomEnchantmentMultiplier multiplier();
    public abstract int maxLevel();
    public abstract List<CustomEnchantmentLevel> levels();
    public abstract String info();
    /* Solid result */
    public abstract List<String> conflicts();

    public LinkedHashMap<String, Object> getDefaultConfig() {
        LinkedHashMap<String, Object> defaults = new LinkedHashMap<>();

        defaults.put("Name", name());
        defaults.put("Description", description());
        defaults.put("Applicable", applicable());
        defaults.put("Treasure", treasure());
        defaults.put("Cursed", cursed());
        defaults.put("Multiplier.Book", multiplier().bookMultiplier);
        defaults.put("Multiplier.Item", multiplier().itemMultiplier);
        defaults.put("MaxLevel", maxLevel());
        defaults.put("LevelInfo", info());

        List<CustomEnchantmentLevel> levels = levels();
        for (CustomEnchantmentLevel level : levels) {
            LinkedHashMap<String, Object> levelDetails = level.getLevelConfig();
            for (Map.Entry<String, Object> entry : levelDetails.entrySet())
                defaults.put("Levels." + level.level + "." + entry.getKey(), entry.getValue());
        }

        return defaults;
    }
}
