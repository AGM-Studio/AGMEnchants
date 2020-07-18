package me.ashenguard.agmenchants.enchants;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;

public class Loots implements Listener {
    @EventHandler
    public void onGeneration(ChunkPopulateEvent e) {
        BlockState[] tileEntities = e.getChunk().getTileEntities();

        for(BlockState state : tileEntities) {
            if(state.getType() == Material.CHEST) {
                ItemStack book =  EnchantmentManager.randomBook();
                if (book == null) continue;

                Chest chest = (Chest) state.getBlock();
                chest.getBlockInventory().addItem(book);
                chest.update(true);
            }
        }
    }
}
