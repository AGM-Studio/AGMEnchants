package me.ashenguard.agmenchants;

import me.ashenguard.api.placeholder.PHExtension;
import me.ashenguard.api.placeholder.Placeholder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedReturnValue")
public class Placeholders extends PHExtension {
    @Override
    public @NotNull String getIdentifier() {
        return "AGMEnchants";
    }

    public Placeholders(){
        super(AGMEnchants.getInstance());

        new Placeholder(this, "total_enchants", ((player, s) -> String.valueOf(AGMEnchants.getEnchantManager().STORAGE.size())));
        new Placeholder(this, "total_runes", ((player, s) -> String.valueOf(AGMEnchants.getRuneManager().STORAGE.size())));
    }
}
