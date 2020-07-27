package me.ashenguard.agmenchants.api.gui;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.api.Messenger;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GUI implements Listener {
    // ---- Constructor ---- //
    private final boolean legacy;
    private File configFile;
    public YamlConfiguration config;

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException exception) {
            Messenger.ExceptionHandler(exception);
        }
    }

    private void setDefaults() {
        AGMEnchants.getInstance().saveResource("GUI.yml", true);
        config = YamlConfiguration.loadConfiguration(configFile);

        if (legacy) {
            config.set("GUI.TopBorder.Material.ID", "STAINED_GLASS_PANE");
            config.set("GUI.TopBorder.Material.Value", 4);
            config.set("GUI.BottomBorder.Material.ID", "STAINED_GLASS_PANE");
            config.set("GUI.BottomBorder.Material.Value", 14);
        }

        saveConfig();
    }

    public GUI(boolean legacy, JavaPlugin plugin) {
        this.legacy = legacy;

        configFile = new File(AGMEnchants.getPluginFolder(), "GUI.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        if (!configFile.exists()) setDefaults();

        Messenger.Debug("GUI", "§5GUI§r has been loaded");

        // ---- Register Listener ---- //
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        Messenger.Debug("GUI", "Listener has been registered");
    }

    // ---- GUI Inventories ---- //
    private HashMap<Player, GUIInventory> inventoryHashMap = new HashMap<>();

    public void saveGUIInventory(Player player, GUIInventory inventory) {
        inventoryHashMap.put(player, inventory);
    }

    public void removeGUIInventory(Player player) {
        inventoryHashMap.remove(player);
    }


    // <editor-fold ---- Item Creators ---- //>
    /**
     * This Method generate item using items enum
     *
     * @param player target player that item will be generated for
     * @param item   grab section from this path in config
     */
    public ItemStack getItemStack(OfflinePlayer player, Items item) {
        ConfigurationSection section = config.getConfigurationSection(item.getPath());

        if (!section.contains("Material")) {
            section.set("Material.ID", item.getID());
            section.set("Material.Value", item.getID().equals("Custom_Head") || item.getID().equals("Player_Head") ? item.getValue() : item.getData());
        }

        return getItemStack(player, section);
    }

    /**
     * This Method generate item using a configuration section
     *
     * @param player target player that item will be generated for
     * @param path   grab section from this path in config
     */
    public ItemStack getItemStack(OfflinePlayer player, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        return getItemStack(player, section);
    }

    /**
     * This Method generate item using a configuration section
     *
     * @param player               target player that item will be generated for
     * @param configurationSection this section should use the following pattern to work
     *                             {@code
     *                             Material.ID: <<Material ID>>
     *                             Material.Value: <<Data for vanilla material/ value for Player_Head and Custom_Head>>
     *                             Name: <<Item name>>
     *                             Lore: <<Item lore>>
     *                             Glow: <<True if item should glow>>
     *                             }
     */
    public ItemStack getItemStack(OfflinePlayer player, ConfigurationSection configurationSection) {
        String ID = configurationSection.getString("Material.ID");
        String value = configurationSection.getString("Material.Value");
        int intValue = configurationSection.getInt("Material.Value", 0);
        String name = configurationSection.getString("Name", "§r");
        List<String> oldLore = configurationSection.getStringList("Lore");
        boolean glow = configurationSection.getBoolean("Glow", false);

        return getItemStack(player, ID, value, (short) intValue, name, oldLore, glow);
    }

    /**
     * This Method generate item using other provided methods
     *
     * @param player   target player that item will be generated for
     * @param name     item name; It will be translated for target player
     * @param lore     item lore; It will be translated for target player
     * @param ID       the item material ID; It can be Custom_Head, Player_Head or a vanilla material
     * @param value    the item value; It would be used in creating Player_Head as Player's name or self to use target player or skin value for Custom_Head
     * @param intValue the data value; It would be used in vanilla material and legacy version
     */
    public ItemStack getItemStack(OfflinePlayer player, String ID, String value, short intValue, String name, List<String> lore) {
        ItemStack item = getItemStack(player, ID, value, intValue);
        return getItemStack(item, player, name, lore);
    }

    public ItemStack getItemStack(OfflinePlayer player, String ID, String value, short intValue, String name, List<String> lore, boolean glow) {
        ItemStack item = getItemStack(player, ID, value, intValue);
        return getItemStack(item, player, name, lore, glow);
    }


    /**
     * This Method give name and lore (and glow) to item given
     *
     * @param player    target player that item will be generated for
     * @param name      item name; It will be translated for target player
     * @param lore      item lore; It will be translated for target player
     * @param itemStack the item; name and lore will be set for this item
     */
    public ItemStack getItemStack(@NotNull ItemStack itemStack, OfflinePlayer player, String name, List<String> lore) {
        name = AGMEnchants.PAPI.translate(player, name);
        lore = AGMEnchants.PAPI.translate(player, lore);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack getItemStack(@NotNull ItemStack itemStack, OfflinePlayer player, String name, List<String> lore, boolean glow) {
        ItemMeta itemMeta = getItemStack(itemStack, player, name, lore).getItemMeta();
        if (glow) {
            itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }


    /**
     * This Method only generate a bare Item/Head based on ID and value
     *
     * @param player   target player that item will be generated for
     * @param ID       the item material ID; It can be Custom_Head, Player_Head or a vanilla material
     * @param value    the item value; It would be used in creating Player_Head as Player's name or self to use target player or skin value for Custom_Head
     * @param intValue the data value; It would be used in vanilla material and legacy version
     */
    public ItemStack getItemStack(OfflinePlayer player, @NotNull String ID, String value, short intValue) {
        if (ID.equals("Custom_Head") || ID.equals("Player_Head"))
            return getItemHead(player, ID.equals("Custom_Head"), value);
        return getItemStack(ID, intValue);
    }

    /**
     * This Method only generate a bare Item based on ID and value
     *
     * @param ID   the item material ID; It can be Custom_Head, Player_Head or a vanilla material
     * @param data the data value; It would be used for legacy version
     */
    public ItemStack getItemStack(String ID, short data) {
        ItemStack item = XMaterial.matchXMaterial(ID).orElse(XMaterial.STONE).parseItem();
        if (item == null) item = new ItemStack(Material.STONE);

        if (legacy) item.setDurability(data);

        return item;
    }

    /**
     * This Method only generate a bare head based on value
     *
     * @param player target player that item will be generated for.
     * @param custom is head a custom head or a player head.
     * @param value  skin value for custom head or value for player head as player's name or self to use target player
     */
    public ItemStack getItemHead(OfflinePlayer player, boolean custom, String value) {
        ItemStack item = XMaterial.PLAYER_HEAD.parseItem();

        if (custom) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            SkullUtils.getSkullByValue(skullMeta, value);
            item.setItemMeta(skullMeta);
        } else {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            if (value.equals("self"))
                skullMeta.setOwningPlayer(player);
            else
                skullMeta.setOwner(value);

            item.setItemMeta(skullMeta);
        }

        return item;
    }
    // </editor-fold>

    /**
     * This event handler detect clicks on a GUIInventory and pass event to Click method in {@link GUIInventory}
     *
     * @param event Inventory Click Event
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        GUIInventory guiInventory = inventoryHashMap.getOrDefault(player, null);
        if (guiInventory == null) return;

        event.setCancelled(true);
        guiInventory.click(event);
    }

    /**
     * This event handler will detect close event and remove it from hash map
     *
     * @param event Inventory Close Event
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        GUIInventory guiInventory = inventoryHashMap.getOrDefault(player, null);
        if (guiInventory == null) return;

        removeGUIInventory(player);
        Messenger.Debug("GUI", "Inventory close detected", "Player= §6" + player.getName(), "Inventory= §6" + guiInventory.title);
    }


    /**
     * Call this on plugin disable, It will close all open inventories
     */
    public void closeAll() {
        for (GUIInventory guiInventory : inventoryHashMap.values()) {
            guiInventory.close();
        }
    }
}

