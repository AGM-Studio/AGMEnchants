package me.ashenguard.agmenchants.classes.gui;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class GUIInventory {
    public String title;
    protected Inventory inventory;
    protected Player player;
    protected GUI gui = AGMEnchants.getGui();

    protected GUIInventory(String title, Player player, Inventory inventory) {
        this.title = title;
        this.player = player;
        this.inventory = inventory;

        gui.saveGUIInventory(player, this);
    }

    public void show() {
        player.openInventory(inventory);
    }

    public void close() {
        player.closeInventory();
        gui.removeGUIInventory(player);
    }

    public void reload() {
        design();
    };

    protected abstract void design();
    public abstract void click(InventoryClickEvent event);


}
