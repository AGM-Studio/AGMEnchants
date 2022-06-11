package me.ashenguard.agmenchants.gui;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.Describable;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.gui.GUIInventorySlot;
import me.ashenguard.api.itemstack.placeholder.PlaceholderItemStack;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.utils.encoding.Roman;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PreviewGUI extends GUIInventory {
    private static final Configuration config = new Configuration(AGMEnchants.getInstance(), "GUI/preview.yml", true);
    private static final List<Integer> slots = config.getIntegerList("EmptySlots");
    private static final Alignment alignment = Alignment.getAlignment(config.getString("Alignment"));

    protected PreviewGUI(Player player, Describable describable) {
        super(player, config);

        placeholders.add(new Placeholder("name", (p, s) -> describable.getName()));
        placeholders.add(new Placeholder("colored_name", (p, s) -> describable.getColoredName()));
        placeholders.add(new Placeholder("description", (p, s) -> describable.getDescription()));
        placeholders.add(new Placeholder("max_level", (p, s) -> describable instanceof Enchant && ((Enchant) describable).getMaxLevel() > 1 ? String.valueOf(((Enchant) describable).getMaxLevel()) : ""));
        placeholders.add(new Placeholder("max_level_roman", (p, s) -> describable instanceof Enchant && ((Enchant) describable).getMaxLevel() > 1 ? Roman.to(((Enchant) describable).getMaxLevel()) : ""));

        List<ItemStack> items = describable.getInfoItems();
        List<Integer> slots = getInfoSlots(items.size());

        for (int i = 0; i < items.size(); i++) {
            int index = slots.get(i);
            ItemStack item = items.get(i);
            GUIInventorySlot slot = new GUIInventorySlot(index).addItem(PlaceholderItemStack.fromItemStack(item));
            if (player.hasPermission("AGMEnchants.admin"))
                slot.setAction(event -> {
                    player.getInventory().addItem(item);
                    return true;
                });
            setSlot(index, slot);
        }
    }

    @Override
    protected Function<InventoryClickEvent, Boolean> getSlotActionByKey(String s) {
        return null;
    }

    private List<Integer> getInfoSlots(int size) {
        List<Integer> slots = new ArrayList<>(PreviewGUI.slots);
        if (alignment == Alignment.RIGHT) Collections.reverse(slots);
        if (alignment != Alignment.CENTER) return slots.subList(0, size);

        int start = (slots.size() - size) / 2;
        int end = (slots.size() - size) / 2 + size;
        // If the size is even (and slots are odd) removing the middle slot will center it
        if (size % 2 == 0 && slots.size() % 2 == 1) slots.remove((slots.size() + 1) / 2);
        return slots.subList(Math.max(0, start), Math.min(slots.size(), end));
    }

    private enum Alignment {
        LEFT, CENTER, RIGHT;

        public static Alignment getAlignment(String value) {
            if (value == null) return CENTER;
            return switch (value.toUpperCase()) {
                case "RIGHT" -> RIGHT;
                case "LEFT" -> LEFT;
                default -> CENTER;
            };
        }
    }
}
