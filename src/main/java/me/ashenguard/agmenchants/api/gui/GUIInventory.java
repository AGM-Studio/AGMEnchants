package me.ashenguard.agmenchants.api.gui;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class GUIInventory {
    public String title;
    protected Inventory inventory;
    protected Player player;
    protected GUI gui = AGMEnchants.GUI;

    protected GUIInventory(String title, Player player, Inventory inventory) {
        this.title = title;
        this.player = player;
        this.inventory = inventory;

        gui.saveGUIInventory(player, this);
    }

    protected GUIInventory(String title, Player player, int size) {
        this(title, player, Bukkit.createInventory(player, size, title));
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
