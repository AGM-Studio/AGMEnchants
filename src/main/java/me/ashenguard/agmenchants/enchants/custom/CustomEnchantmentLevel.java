package me.ashenguard.agmenchants.enchants.custom;

import java.util.LinkedHashMap;

public abstract class CustomEnchantmentLevel {
    public final int level;

    protected CustomEnchantmentLevel(int level) {
        this.level = level;
    }

    public abstract LinkedHashMap<String, Object> getLevelDetails();
}
