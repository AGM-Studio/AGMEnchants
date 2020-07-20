package me.ashenguard.agmenchants.api;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateChecker {

    private Plugin plugin;
    private int resourceId;

    public UpdateChecker(Plugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if (resourceId == 0) {
                consumer.accept("1.0");
                return;
            }
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                Messenger.Warning("Cannot look for updates: " + exception.getMessage());
            }
        });
    }

    public boolean checkVersion() {
        return !AGMEnchants.getInstance().getDescription().getVersion().equalsIgnoreCase(getVersion());
    }

    public String getVersion() {
        AtomicReference<String> newVersion = new AtomicReference<>("1.0");
        getVersion(newVersion::set);
        return newVersion.get();
    }
}