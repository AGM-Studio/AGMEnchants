package me.ashenguard.agmenchants.enchants.custom;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class CustomEnchantmentDefaultValues {
    protected abstract String name();
    protected abstract String description();
    protected abstract List<String> applicable();
    protected abstract boolean treasure();
    protected abstract boolean cursed();
    protected abstract CustomEnchantmentMultiplier multiplier();
    protected abstract int maxLevel();
    protected abstract List<CustomEnchantmentLevel> levels();
    protected abstract String info();
    protected abstract List<String> conflicts();

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
        defaults.put("Conflicts", conflicts());

        List<CustomEnchantmentLevel> levels = levels();
        for (CustomEnchantmentLevel level : levels) {
            LinkedHashMap<String, Object> levelDetails = level.getLevelConfig();
            for (Map.Entry<String, Object> entry : levelDetails.entrySet())
                defaults.put("Levels." + level.level + "." + entry.getKey(), entry.getValue());
        }

        return defaults;
    }
}
