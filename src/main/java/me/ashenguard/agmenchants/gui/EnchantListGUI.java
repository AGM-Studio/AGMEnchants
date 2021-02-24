package me.ashenguard.agmenchants.gui;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.enchants.EnchantManager;
import me.ashenguard.api.gui.GUIInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EnchantListGUI extends GUIInventory {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();

    private int page;
    private final List<Enchant> list = ENCHANT_MANAGER.STORAGE.getAll();

    public EnchantListGUI(Player player) {
        super(PLUGIN.GUI, PLUGIN.getConfig().getString("Prefix"), player, 54);

        page = 0;
    }

    @Override
    protected void design() {
        for (int i=0; i<9; i++)
            inventory.setItem(i, GUI.getItemStack(player, Items.TopBorder));

        for (int i=45; i<54; i++)
            inventory.setItem(i, GUI.getItemStack(player, Items.BottomBorder));

        if (page > 0)
            inventory.setItem(45, GUI.getItemStack(player, Items.LeftButton));
        if (page * 36 + 36 < list.size())
            inventory.setItem(53, GUI.getItemStack(player, Items.RightButton));

        for (int i = 0; i < 36; i++) {
            try {
                Enchant enchant = list.get(i + page * 36);
                ItemStack item = enchant.getInfoBook();
                inventory.setItem(9 + i, item);
            } catch (IndexOutOfBoundsException ignored) {
                inventory.setItem(9 + i, null);
            }
        }
    }

    @Override
    public void click(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 45) this.left();
        else if (slot == 53) this.right();
        else if (slot < 45 && slot > 8) {
            try {
                EnchantGUI enchantGUI = new EnchantGUI(this, player, list.get(slot - 9 + page * 36));
                this.close();
                enchantGUI.show();
            } catch (IndexOutOfBoundsException ignored) {}
        }
    }

    private void left() {
        page = Math.max(0, page - 1);
        reload();
    }

    private void right() {
        if (page * 36 + 36 < list.size()) page += 1;
        reload();
    }
}