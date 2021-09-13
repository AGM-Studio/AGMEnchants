package me.ashenguard.lib.events;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.runes.Rune;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RuneInteractEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Rune rune;
    private boolean consume = true;
    private final ItemStack item;
    private boolean cancelled = false;

    @Override public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public RuneInteractEvent(@NotNull Player who, @NotNull Rune rune, ItemStack item) {
        super(who);
        this.rune = rune;
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public Rune getRune() {
        return rune;
    }

    public boolean willConsume() {
        return consume;
    }

    public void setConsume(boolean consume) {
        this.consume = consume;
    }

    public boolean isOriginal() {
        return AGMEnchants.getRuneManager().isItemRune(item);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
