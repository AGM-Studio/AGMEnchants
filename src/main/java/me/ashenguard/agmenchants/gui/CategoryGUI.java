package me.ashenguard.agmenchants.gui;

import com.cryptomorin.xseries.XMaterial;
import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.classes.Enchant;
import me.ashenguard.agmenchants.classes.Rune;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CategoryGUI extends GUIInventory {
    public CategoryGUI(Player player) {
        super(player, new Configuration(AGMEnchants.getInstance(), "GUI/categories.yml", true));
    }

    private static final Map<String, Collection<String>> actionMap = new HashMap<>();
    static {
        actionMap.put("Helmet", Arrays.asList("HELMETS", "HELMET", "IRON_HELMET"));
        actionMap.put("Chestplate", Arrays.asList("CHESTPLATES", "CHESTPLATE", "IRON_CHESTPLATE"));
        actionMap.put("Leggings", Arrays.asList("LEGGINGS", "IRON_LEGGINGS"));
        actionMap.put("Boots", Arrays.asList("BOOTS", "IRON_BOOTS"));

        actionMap.put("Pickaxe", Arrays.asList("PICKAXES", "PICKAXE", "IRON_PICKAXE"));
        actionMap.put("Axe", Arrays.asList("AXES", "AXE", "IRON_AXE"));
        actionMap.put("Shovel", Arrays.asList("SHOVELS", "SHOVEL", "IRON_SHOVEL"));
        actionMap.put("Hoe", Arrays.asList("HOES", "HOE", "IRON_HOE"));

        actionMap.put("Sword", Arrays.asList("SWORDS", "SWORD", "IRON_SWORD"));
        actionMap.put("Shield", Collections.singletonList("SHIELD"));
        actionMap.put("Bow", Arrays.asList("BOW", "CROSSBOW"));
        actionMap.put("Trident", Collections.singletonList("TRIDENT"));
    }

    @Override protected Function<InventoryClickEvent, Boolean> getSlotActionByKey(String key) {
        if ("All".equalsIgnoreCase(key)) return event -> openList(EnchantManager.STORAGE.getAll(), RuneManager.STORAGE.getAll());
        Collection<String> list = actionMap.getOrDefault(key, null);
        if (list == null) return null;
        
        return event -> openList(
                EnchantManager.STORAGE.getAll().stream().filter(enchant -> isEnchantApplicable(enchant, list)).collect(Collectors.toList()),
                RuneManager.STORAGE.getAll().stream().filter(rune -> isRuneApplicable(rune, list)).collect(Collectors.toList())
        );
    }

    private boolean openList(List<Enchant> enchants, List<Rune> runes) {
        this.close();
        new ListGUI(this.getPlayer(), enchants, runes).show();
        return true;
    }
    
    private static boolean isEnchantApplicable(Enchant enchant, Collection<String> list) {
        for (String item: list) {
            try {
                Material material = XMaterial.matchXMaterial(item).orElseThrow().parseMaterial();
                if (material != null && enchant.isApplicable(material)) return true;
            } catch (NoSuchElementException ignored) {
                if (isEnchantApplicable(enchant, AGMEnchants.getMainManager().getGroups().getStringList(item))) return true;
            }
        }
        return false;
    }
    private static boolean isRuneApplicable(Rune rune, Collection<String> list) {
        for (String item: list) {
            try {
                Material material = XMaterial.matchXMaterial(item).orElseThrow().parseMaterial();
                if (material != null && rune.isApplicable(material)) return true;
            } catch (NoSuchElementException ignored) {
                if (isRuneApplicable(rune, AGMEnchants.getMainManager().getGroups().getStringList(item))) return true;
            }
        }
        return false;
    }
}
