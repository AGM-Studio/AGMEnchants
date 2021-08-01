package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.placeholder.Placeholder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class VanillaEnchant extends Enchant{
    private final Enchantment enchantment;

    private static Configuration getConfiguration(String name) {
        AGMEnchants plugin = AGMEnchants.getInstance();
        String path = String.format("Enchants/vanilla/%s.yml", name);
        InputStream resource = plugin.getResource(String.format("Vanilla/%s.yml", name));
        return new Configuration(plugin, path, resource);
    }

    public VanillaEnchant(Enchantment enchantment) {
        super(enchantment.getName(), getConfiguration(enchantment.getName()));
        this.enchantment = enchantment;
    }

    @Override public boolean isApplicable(Material material) {
        return enchantment.canEnchantItem(new ItemStack(material));
    }
    @Override public boolean conflictsWith(Enchant enchant) {
        if (enchant instanceof VanillaEnchant) {
            VanillaEnchant vanillaEnchant = (VanillaEnchant) enchant;
            return enchantment.conflictsWith(vanillaEnchant.enchantment);
        }
        return enchant.conflictsWith(this);
    }
    @Override public int getMaxLevel() {
        return enchantment.getMaxLevel();
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
        return enchantment.isCursed();
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    @Override public String toString() {
        return String.format("%s[ID=%s, Name=%s]", "VanillaEnchant", ID, getName());
    }
}
