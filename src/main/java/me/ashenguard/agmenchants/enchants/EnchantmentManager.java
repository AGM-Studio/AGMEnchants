package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.agmclasses.AGMMessenger;
import me.ashenguard.agmenchants.classes.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public final class EnchantmentManager {
    private static HashMap<String, CustomEnchantment> enchantmentHashMap = new HashMap<>();
    private final JavaPlugin plugin;

    public EnchantmentManager(JavaPlugin instance) {
        plugin = instance;
    }

    @Nullable
    public static ItemStack randomBook() {
        if (new Random().nextBoolean()) return null;
        List<CustomEnchantment> enchantments = (List<CustomEnchantment>) enchantmentHashMap.values();
        return enchantments.get(new Random().nextInt(enchantments.size())).getBook();
    }

    public boolean registerEnchantment(CustomEnchantment enchantment) {
        if (enchantment == null || enchantment.getName() == null) return false;

        if (enchantment instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) enchantment, plugin);
        }

        AGMMessenger.Info("Successfully registered expansion: " + enchantment.getName());

        return true;
    }

    public CustomEnchantment registerEnchantment(String fileName) {
        List<Class<?>> subs = FileUtil.getClasses(AGMEnchants.getEnchantsFolder(), fileName, CustomEnchantment.class);
        if (subs == null || subs.isEmpty()) return null;

        CustomEnchantment enchantment = createInstance(subs.get(0));
        if (registerEnchantment(enchantment)) return enchantment;
        return null;
    }

    public void registerAllEnchantments() {
        List<Class<?>> subs = FileUtil.getClasses(AGMEnchants.getEnchantsFolder(), CustomEnchantment.class);
        System.out.println(subs);
        if (subs == null || subs.isEmpty()) return;

        for (Class<?> klass : subs) {
            CustomEnchantment enchantment = createInstance(klass);
            if (enchantment != null) {
                try {
                    registerEnchantment(enchantment);
                } catch (Exception e) {
                    AGMMessenger.Info("Couldn't register " + enchantment.getName() + " enchantment");
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
            AGMMessenger.Warning("Failed to init enchantment from class: " + clazz.getName());
            AGMMessenger.Warning(t.getMessage());
        }

        return expansion;
    }
}
