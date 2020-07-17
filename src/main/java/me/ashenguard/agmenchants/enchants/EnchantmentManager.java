package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.agmclasses.AGMMessenger;
import me.clip.placeholderapi.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.List;

public final class EnchantmentManager {
    private final JavaPlugin plugin;

    public EnchantmentManager(JavaPlugin instance) {
        plugin = instance;
    }

    public boolean registerEnchantment(Enchantment enchantment) {
        if (enchantment == null || enchantment.getName() == null) return false;

        if (enchantment instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) enchantment, plugin);
        }

        AGMMessenger.Info("Successfully registered expansion: " + enchantment.getName());

        return true;
    }

    public Enchantment registerEnchantment(String fileName) {
        List<Class<?>> subs = FileUtil.getClasses("expansions", fileName, Enchantment.class);
        if (subs == null || subs.isEmpty()) return null;

        Enchantment enchantment = createInstance(subs.get(0));
        if (registerEnchantment(enchantment)) return enchantment;
        return null;
    }

    public void registerAllEnchantments() {
        List<Class<?>> subs = FileUtil.getClasses("expansions", null, Enchantment.class);
        if (subs == null || subs.isEmpty()) return;

        for (Class<?> klass : subs) {
            Enchantment enchantment = createInstance(klass);
            if (enchantment != null) {
                try {
                    registerEnchantment(enchantment);
                } catch (Exception e) {
                    AGMMessenger.Info("Couldn't register " + enchantment.getName() + " expansion");
                    e.printStackTrace();
                }
            }
        }
    }

    private Enchantment createInstance(Class<?> clazz) {
        if (clazz == null) return null;
        if (!Enchantment.class.isAssignableFrom(clazz)) return null;

        Enchantment expansion = null;
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            if (constructors.length == 0) {
                expansion = (Enchantment) clazz.newInstance();
            } else {
                for (Constructor<?> ctor : constructors) {
                    if (ctor.getParameterTypes().length == 0) {
                        expansion = (Enchantment) ctor.newInstance();
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            AGMMessenger.Warning("Failed to init enchantment from class: " + clazz.getName());
            AGMMessenger.Warning(t.getMessage());
        }

        return expansion;
    }
}
