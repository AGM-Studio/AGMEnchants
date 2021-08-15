package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.placeholder.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class VanillaEnchant extends Enchant{
    private final Enchantment enchantment;

    private static Configuration getConfiguration(String name) {
        AGMEnchants plugin = AGMEnchants.getInstance();
        String path = String.format("Enchants/vanilla/%s.yml", name);
        InputStream resource = plugin.getResource(String.format("Vanilla/%s.yml", name));
        return new Configuration(plugin, path, resource);
    }

    public VanillaEnchant(Enchantment enchantment) {
        super(enchantment.getKey(), getConfiguration(enchantment.getKey().getKey()));
        this.enchantment = enchantment;
    }

    @Override public void unregister() {}

    @Override public boolean isApplicable(Material material) {
        return enchantment.canEnchantItem(new ItemStack(material));
    }
    @Override public @NotNull EnchantmentTarget getItemTarget() {
        return enchantment.getItemTarget();
    }
    @Override public boolean conflictsWith(@NotNull Enchant enchant) {
        if (enchant instanceof VanillaEnchant) {
            VanillaEnchant vanillaEnchant = (VanillaEnchant) enchant;
            return enchantment.conflictsWith(vanillaEnchant.enchantment);
        }
        return enchant.conflictsWith(this);
    }

    @Override public int getMaxLevel() {
        return enchantment.getMaxLevel();
    }
    @Override public int getStartLevel() {
        return enchantment.getStartLevel();
    }
    @Override public boolean isSafe(int level) {
        return true;
    }
    @Override public void onEnchanting(ItemStack item, int level) {}
    @Override public void onDisenchanting(ItemStack item, int level) {}
    @Override public List<Placeholder> getPlaceholders(int level) {
        return new ArrayList<>();
    }
    @Override public boolean isTreasure() {
        return enchantment.isTreasure();
    }
    @Override public boolean isCursed() {
        return enchantment.getKey().equals(NamespacedKey.minecraft("binding_curse")) || enchantment.getKey().equals(NamespacedKey.minecraft("vanishing_curse"));
    }

    @Override
    public boolean canBeTraded() {
        return !enchantment.getKey().equals(NamespacedKey.minecraft("soul_speed"));
    }
    @Override
    public boolean canBeBartered() {
        return enchantment.getKey().equals(NamespacedKey.minecraft("soul_speed"));
    }
    @Override
    public boolean canBeFished() {
        return !enchantment.getKey().equals(NamespacedKey.minecraft("soul_speed"));
    }
    @Override
    public boolean canBeLooted(World world) {
        if (!enchantment.getKey().equals(NamespacedKey.minecraft("soul_speed"))) return true;
        return world.getEnvironment().equals(World.Environment.NETHER);
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }
}
