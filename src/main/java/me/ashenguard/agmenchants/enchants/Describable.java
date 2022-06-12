package me.ashenguard.agmenchants.enchants;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Describable {
    String getName();
    String getColoredName();
    String getDescription();

    ItemStack getInfoItem();
    List<ItemStack> getInfoItems();
}
