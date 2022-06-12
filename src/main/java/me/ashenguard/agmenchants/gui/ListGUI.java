package me.ashenguard.agmenchants.gui;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.Rune;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.gui.GUIInventorySlot;
import me.ashenguard.api.gui.GUIUpdater;
import me.ashenguard.api.itemstack.placeholder.PlaceholderItemStack;
import me.ashenguard.api.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.function.Function;

public class ListGUI extends GUIInventory {
    private static final Configuration config = new Configuration(AGMEnchants.getInstance(), "GUI/list.yml", true);
    private static final List<Integer> slots = config.getIntegerList("EmptySlots");

    private final List<Enchant> enchants;
    private final List<Rune> runes;

    private int page = 0;

    public ListGUI(Player player, List<Enchant> enchants, List<Rune> runes) {
        super(player, config);
        this.enchants = enchants;
        this.runes = runes;

        placeholders.add(new Placeholder("page", (p, s) -> String.valueOf(page)));
        placeholders.add(new Placeholder("total_pages", (p, s) -> String.valueOf(page)));
    }

    protected void update() {
        for (int i = slots.size() * page; i < slots.size() * (page + 1) - 1; i++) {
            int index = slots.get(i - slots.size() * page);
            if (i < runes.size()) {
                Rune rune = runes.get(i);
                GUIInventorySlot slot = new GUIInventorySlot(index).addItem(PlaceholderItemStack.fromItemStack(rune.getInfoItem()));
                slot.setAction(event -> {
                    new PreviewGUI(player, rune).show();
                    return true;
                });
                setSlot(index, slot);
            } else if (i < runes.size() + enchants.size()) {
                Enchant enchant = enchants.get(i - runes.size());
                GUIInventorySlot slot = new GUIInventorySlot(index).addItem(PlaceholderItemStack.fromItemStack(enchants.get(i).getInfoItem()));
                slot.setAction(event -> {
                   new PreviewGUI(player, enchant).show();
                   return true;
                });
                setSlot(index, slot);
            } else {
                setSlot(index, null);
            }
        }
    }

    @Override
    protected Function<InventoryClickEvent, Boolean> getSlotActionByKey(String key) {
        return switch (key) {
            case "MenuPage" -> event -> {
                new CategoryGUI(player).show();
                return true;
            };
            case "PreviousPage" -> event -> {
                if (page > 0) page--;
                this.update();
                GUIUpdater.update(this);
                return true;
            };
            case "NextPage" -> event -> {
                if ((page + 1) * slots.size() > runes.size() + enchants.size()) page++;
                this.update();
                GUIUpdater.update(this);
                return true;
            };
            default -> null;
        };
    }
}
