package me.ashenguard.agmenchants.gui;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.api.gui.GUIInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EnchantGUI extends GUIInventory {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();

    private final EnchantListGUI menu;
    private final Enchant enchant;
    private int page;

    protected EnchantGUI(EnchantListGUI menu, Player player, Enchant enchant) {
        super(PLUGIN.GUI, PLUGIN.getConfig().getString("Prefix") + " §r- " + enchant.getColoredName(), player, 27);

        this.menu = menu;
        this.enchant = enchant;
        this.page = 0;
    }

    @Override
    protected void design() {
        for (int i=0; i<9; i++)
            inventory.setItem(i, GUI.getItemStack(player, Items.TopBorder));

        for (int i=18; i<27; i++)
            inventory.setItem(i, GUI.getItemStack(player, Items.BottomBorder));

        if (page > 0)
            inventory.setItem(18, GUI.getItemStack(player, Items.LeftButton));
        if (page * 9 + 9 < enchant.getMaxLevel())
            inventory.setItem(27, GUI.getItemStack(player, Items.RightButton));
        inventory.setItem(22, GUI.getItemStack(player, Items.Return));

        for (int i = 0; i < 9; i++) {
            if (i + page * 9 < enchant.getMaxLevel()) inventory.setItem(i + 9, enchant.getEnchantedBook(i + 1 + page * 9));
            else inventory.setItem(i + 9, null);
        }
    }

    @Override
    public void click(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 18) this.left();
        else if (slot == 27) this.right();
        else if (slot == 22) {
            this.close(); menu.show();
        } else if (slot < 18 && slot > 8 && player.hasPermission("AGMEnchants.admin")) {
            if (slot - 9 < enchant.getMaxLevel())
                player.getInventory().addItem(enchant.getEnchantedBook(slot - 8));
        }
    }

    private void left() {
        page = Math.max(0, page - 1);
        reload();
    }

    private void right() {
        if (page * 9 + 9 < enchant.getMaxLevel()) page += 1;
        reload();
    }
}
