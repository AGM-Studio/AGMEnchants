package me.ashenguard.agmenchants.api.gui.inventories;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.api.gui.GUIInventory;
import me.ashenguard.agmenchants.api.gui.Items;
import me.ashenguard.agmenchants.enchants.CustomEnchantment;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class EnchantsGUI extends GUIInventory {
    private int page;
    private CustomEnchantment selected;
    private List<CustomEnchantment> enchantments = new ArrayList<>();

    public EnchantsGUI(Player player) {
        super(AGMEnchants.config.getString("Prefix"), player, 54);
        gui.saveGUIInventory(player, this);

        page = 0;
        selected = null;

        reload();
    }

    @Override
    protected void design() {
        for (int i=0; i<9; i++)
            inventory.setItem(i, gui.getItemStack(null, Items.TopBorder));

        for (int i=45; i<53; i++)
            inventory.setItem(i, gui.getItemStack(null, Items.BottomBorder));

        inventory.setItem(45, gui.getItemStack(player, Items.LeftButton));
        inventory.setItem(53, gui.getItemStack(player, Items.RightButton));

        if (selected == null) addEnchants();
        else showLevels();
    }

    @Override
    public void click(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 45) this.left();
        else if (slot == 53) this.right();
        else if (slot < 45 && slot > 8) {
            if (selected == null) {
                selected = enchantments.get(slot - 9);
                reload();
            } else if (slot - 9 < selected.getMaxLevel() && player.hasPermission("AGMEnchants.admin"))
                player.getInventory().addItem(selected.getBook(slot - 9));
        }
    }

    // ------------------------------------------------------

    private void left() {
        page = Math.max(0, page - 1);
        reload();
    }

    private void right() {
        int count = EnchantmentManager.getEnchantments().size();
        if (page * 36 < count) return;

        page += 1;
        reload();
    }

    private void addEnchants() {
        List<String> enchants = new ArrayList<>(EnchantmentManager.getEnchantments());
        for (int i = 0; i < 36; i++) {
            if (page * 36 + i > enchants.size()) {
                inventory.setItem(i + 9, null);
                enchantments.set(i, null);
                continue;
            }

            CustomEnchantment customEnchantment = EnchantmentManager.getEnchantment(enchants.get(page * 36 + i));
            inventory.setItem(i + 9, customEnchantment.getInfoBook());
            enchantments.set(i, customEnchantment);
        }
    }

    private void showLevels() {
        for (int i = 0; i < 36; i++) {
            if (i < selected.getMaxLevel()) inventory.setItem(i + 9, selected.getInfoBook(i + 1));
            else inventory.setItem(i + 9, null);
        }
    }
}
