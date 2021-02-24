package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.spigot.SpigotPlugin;
import me.ashenguard.api.utils.FileUtils;
import me.ashenguard.api.versions.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"SameReturnValue", "unused"})
public abstract class CustomEnchant extends Enchant implements Listener {
    private final Version version;

    private static Configuration getConfiguration(String ID, File JAR) {
        SpigotPlugin plugin = AGMEnchants.getInstance();
        String path = String.format("Enchants/configs/%s.yml", ID);

        return new Configuration(plugin, path, FileUtils.getResource(JAR, "config.yml"));
    }

    public boolean canBeRegistered() {
        return true;
    }
    @Override public boolean register() {
        Enchant exists = ENCHANT_MANAGER.STORAGE.get(ID);
        if (exists != null) return false;
        ENCHANT_MANAGER.STORAGE.save(this);
        Bukkit.getPluginManager().registerEvents(this, PLUGIN);
        MESSENGER.Debug("Enchants", "Enchantment has been registered.", "Enchantment= §6" + toString());
        return true;
    }

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
        super(ID, getConfiguration(ID, JAR));
        this.version = version;
    }

    @Override public String toString() {
        return String.format("%s[ID=%s, Name=%s]", "CustomEnchant", ID, getName());
    }

    public Version getVersion() {
        return version;
    }
}
