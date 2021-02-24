package me.ashenguard.agmenchants;

import me.ashenguard.api.placeholder.PHExtension;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedReturnValue")
public class Placeholders extends PHExtension {
    @Override
    public @NotNull String getIdentifier() {
        return "AGMRanks";
    }

    public Placeholders(){
        super(AGMEnchants.getInstance());
    }
}
