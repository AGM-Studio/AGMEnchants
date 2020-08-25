package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantment;
import me.ashenguard.api.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.List;

public class EnchantmentLoader {
    private final JavaPlugin plugin;

    public EnchantmentLoader(JavaPlugin instance) {
        plugin = instance;
    }

    public boolean registerEnchantment(CustomEnchantment enchantment) {
        if (enchantment == null || enchantment.getName() == null) return false;

        if (enchantment instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) enchantment, plugin);
        }

        AGMEnchants.Messenger.Debug("Enchants", "Enchantment registered successfully", "Enchantment= §6" + enchantment.getName());
        return true;
    }

    public CustomEnchantment registerEnchantment(String fileName) {
        List<Class<?>> subs = FileUtils.getClasses(AGMEnchants.getEnchantsFolder(), fileName, CustomEnchantment.class);
        if (subs == null || subs.isEmpty()) return null;

        CustomEnchantment enchantment = createInstance(subs.get(0));
        if (registerEnchantment(enchantment)) return enchantment;
        return null;
    }

    public void registerAllEnchantments() {
        List<Class<?>> subs = FileUtils.getClasses(AGMEnchants.getEnchantsFolder(), CustomEnchantment.class);
        if (subs == null || subs.isEmpty()) return;

        for (Class<?> klass : subs) {
            CustomEnchantment enchantment = createInstance(klass);
            if (enchantment != null) {
                try {
                    registerEnchantment(enchantment);
                } catch (Exception e) {
                    AGMEnchants.Messenger.Warning("Unable to register enchantment called " + enchantment.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private CustomEnchantment createInstance(Class<?> clazz) {
        if (clazz == null) return null;
        if (!CustomEnchantment.class.isAssignableFrom(clazz)) return null;

        CustomEnchantment expansion = null;
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            if (constructors.length == 0) {
                expansion = (CustomEnchantment) clazz.newInstance();
            } else {
                for (Constructor<?> ctor : constructors) {
                    if (ctor.getParameterTypes().length == 0) {
                        expansion = (CustomEnchantment) ctor.newInstance();
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            AGMEnchants.Messenger.Warning("Failed to initialize enchantment from class: " + clazz.getName());
            t.printStackTrace();
        }

        return expansion;
    }
}
