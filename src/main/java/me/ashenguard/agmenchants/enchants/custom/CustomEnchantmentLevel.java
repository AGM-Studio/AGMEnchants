package me.ashenguard.agmenchants.enchants.custom;

import java.util.LinkedHashMap;

public abstract class CustomEnchantmentLevel {
    public final int level;

    protected CustomEnchantmentLevel(int level) {
        this.level = level;
    }

    public abstract LinkedHashMap<String, Object> getLevelConfig();
    public LinkedHashMap<String, Object> getLevelDetail() {
        LinkedHashMap<String, Object> details = new LinkedHashMap<>();

        details.put("Level", level);
        details.putAll(getLevelConfig());

        return details;
    }
}
