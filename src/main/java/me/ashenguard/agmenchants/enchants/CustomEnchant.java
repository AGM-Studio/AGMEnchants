package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.spigot.SpigotPlugin;
import me.ashenguard.api.utils.FileUtils;
import me.ashenguard.api.versions.Version;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"SameReturnValue", "unused"})
public abstract class CustomEnchant extends Enchant implements Listener {
    private final Version version;

    private final List<String> applicable;
    private final List<NamespacedKey> conflicts;
    private final EnchantmentTarget target;
    private final boolean treasure;
    private final boolean cursed;
    private final int maxLevel;

    private final boolean trading;
    private final boolean bartering;
    private final boolean fishing;
    private final List<String> loot;

    private static Configuration getConfiguration(String ID, File JAR) {
        SpigotPlugin plugin = AGMEnchants.getInstance();
        String path = String.format("Enchants/configs/%s.yml", ID);

        return new Configuration(plugin, path, FileUtils.getResource(JAR, "config.yml"));
    }

    public boolean canBeRegistered() {
        return true;
    }
    @Override public boolean register() {
        Enchant exists = ENCHANT_MANAGER.STORAGE.get(this.getKey());
        if (exists != null) return false;
        ENCHANT_MANAGER.STORAGE.save(this);
        Bukkit.getPluginManager().registerEvents(this, PLUGIN);
        Enchantment.registerEnchantment(this);
        onRegister();
        MESSENGER.Debug("Enchants", "Enchantment has been registered.", "Enchantment= §6" + toString());

        return true;
    }
    @Override public void unregister() {
        ENCHANT_MANAGER.unregisterEnchantment(this);
        onUnregister();
        MESSENGER.Debug("Enchants", "Enchantment's registration has been removed.", "Enchantment= §6" + toString());
    }

    public void onRegister() {}
    public void onUnregister() {}

    public abstract List<ItemStack> getItemStacks(Entity entity);

    public List<Integer> getItemLevels(Entity entity) {
        List<ItemStack> items = getItemStacks(entity);
        return items.stream().map(this::getLevel).collect(Collectors.toList());
    }
    public int getTotalItemLevel(Entity entity) {
        return getItemLevels(entity).stream().mapToInt(Integer::intValue).sum();
    }
    public int getMaxItemLevel(Entity entity) {
        List<ItemStack> items = getItemStacks(entity);
        return getItemLevels(entity).stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public CustomEnchant(File JAR) {
        this(JAR, JAR.getName().substring(0, JAR.getName().lastIndexOf('.')));
    }
    public CustomEnchant(File JAR, String ID) {
        this(JAR, ID, "1.0");
    }
    public CustomEnchant(File JAR, String ID, String version) {
        this(JAR, ID, new Version(version));
    }
    public CustomEnchant(File JAR, String ID, Version version) {
        this(JAR, new NamespacedKey(AGMEnchants.getInstance(), ID), version);
    }
    public CustomEnchant(File JAR, NamespacedKey ID, Version version) {
        super(ID, getConfiguration(ID.getKey(), JAR));
        this.version = version;

        EnchantmentTarget target;
        try {
            target = EnchantmentTarget.valueOf(config.getString("Target", ""));
        } catch (Throwable ignored) {
            //noinspection deprecation
            target = EnchantmentTarget.ALL;
        }

        this.target = target;
        this.applicable = config.getStringList("Applicable");

        this.conflicts = new ArrayList<>();
        List<String> conflicts = config.getStringList("Conflicts");
        for (String conflict: conflicts) this.conflicts.add(NamespacedKey.fromString(conflict));

        this.maxLevel = config.getInt("MaxLevel", 1);
        this.treasure = config.getBoolean("Treasure", false);
        this.cursed = config.getBoolean("Cursed", false);

        this.trading = config.getBoolean("VillagersTrading", true);
        this.bartering = config.getBoolean("PiglinBartering", false);
        this.fishing = config.getBoolean("Fishing", true);
        this.loot = config.contains("Loot") ? config.getStringList("Loot") : Arrays.asList("OVER_WORLD", "NETHER", "END");
    }

    private boolean isApplicable(String material, String applicable) {
        if (applicable.equalsIgnoreCase(material)) return true;
        List<String> list = AGMEnchants.getMainManager().getGroups().getStringList(applicable);
        for (String name: list) if (isApplicable(material, name)) return true;
        return false;
    }
    @Override public boolean isApplicable(Material material) {
        if (material == null || material.equals(Material.AIR)) return false;

        EnchantmentTarget target = getItemTarget();
        if (!target.toString().equals("ALL")) return target.includes(material);
        if (applicable.contains("EVERYTHING") || applicable.contains("ALL")) return true;
        for (String name: applicable) if (isApplicable(material.name(), name)) return true;
        return false;
    }
    @Override public boolean conflictsWith(@NotNull Enchant enchant) {
        if (enchant instanceof VanillaEnchant)
            return conflicts.contains(enchant.getKey());
        if (enchant instanceof CustomEnchant)
            return conflicts.contains(enchant.getKey()) || ((CustomEnchant) enchant).conflicts.contains(getKey());
        return false;
    }

    @Override public int getMaxLevel() {
        return maxLevel;
    }
    @Override public int getStartLevel() {
        return 1;
    }
    @Override public @NotNull EnchantmentTarget getItemTarget() {
        return target;
    }
    @Override public boolean isTreasure() {
        return treasure;
    }
    @Override public boolean isCursed() {
        return cursed;
    }

    @Override
    public boolean canBeTraded() {
        return trading;
    }
    @Override
    public boolean canBeBartered() {
        return bartering;
    }
    @Override
    public boolean canBeFished() {
        return fishing;
    }
    @Override
    public boolean canBeLooted(World world) {
        for (String name: loot) if (name.equalsIgnoreCase(world.getName())) return true;
        return false;
    }

    public Version getVersion() {
        return version;
    }
}
