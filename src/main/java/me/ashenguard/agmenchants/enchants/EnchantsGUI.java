package me.ashenguard.agmenchants.enchants;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantment;
import me.ashenguard.api.gui.GUIInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class EnchantsGUI extends GUIInventory {
    private int page;
    private CustomEnchantment selected;
    private MenuType type;
    private List<CustomEnchantment> enchantments = new ArrayList<>();

    public EnchantsGUI(Player player) {
        super(AGMEnchants.GUI, AGMEnchants.config.getString("Prefix"), player, 54);
        GUI.saveGUIInventory(player, this);

        page = 0;
        type = MenuType.List;
        selected = null;

        reload();
    }

    @Override
    protected void design() {
        for (int i=0; i<9; i++)
            inventory.setItem(i, GUI.getItemStack(player, Items.TopBorder.getPath()));

        for (int i=45; i<53; i++)
            inventory.setItem(i, GUI.getItemStack(player, Items.BottomBorder.getPath()));

        inventory.setItem(45, GUI.getItemStack(player, Items.LeftButton.getPath()));
        inventory.setItem(53, GUI.getItemStack(player, Items.RightButton.getPath()));

        if (type == MenuType.List) addEnchants();
        else showLevels();
    }

    @Override
    public void click(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 45) this.left();
        else if (slot == 53) this.right();
        else if (slot < 45 && slot > 8) {
            if (type == MenuType.List) {
                selected = enchantments.get(slot - 9);
                type = MenuType.Levels;
                reload();
            } else if (slot - 9 < selected.getMaxLevel() && player.hasPermission("AGMEnchants.admin"))
                player.getInventory().addItem(selected.getBook(slot - 8));
        } else if (slot == 50 && type == MenuType.Levels) {
            page = 0;
            type = MenuType.List;
            selected = null;
            reload();
        }
    }

    // ------------------------------------------------------

    private void left() {
        page = Math.max(0, page - 1);
        reload();
    }

    private void right() {
        int count = EnchantmentManager.getCustomEnchantments().size();
        if (page * 36 < count) return;

        page += 1;
        reload();
    }

    private void addEnchants() {
        enchantments = new ArrayList<>();
        List<String> enchants = new ArrayList<>(EnchantmentManager.getCustomEnchantments());
        for (int i = 0; i < 36; i++) {
            if (page * 36 + i >= enchants.size()) {
                inventory.setItem(i + 9, null);
                enchantments.add(null);
                continue;
            }

            CustomEnchantment customEnchantment = EnchantmentManager.getCustomEnchantment(enchants.get(page * 36 + i));
            inventory.setItem(i + 9, customEnchantment.getInfoBook());
            enchantments.add(customEnchantment);
        }
    }

    private void showLevels() {
        inventory.setItem(50, GUI.getItemStack(player, Items.Return.getPath()));

        for (int i = 0; i < 36; i++) {
            if (i < selected.getMaxLevel()) inventory.setItem(i + 9, selected.getInfoBook(i + 1));
            else inventory.setItem(i + 9, null);
        }
    }
}

enum Items {
    TopBorder,
    BottomBorder,
    LeftButton,
    RightButton,
    Return;

    private String path;

    Items() {
        this.path = "GUI." + this.name();
    }

    public String getPath() {
        return path;
    }
}

enum MenuType {
    List, Levels;
}