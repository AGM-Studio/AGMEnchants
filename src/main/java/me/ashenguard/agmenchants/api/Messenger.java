package me.ashenguard.agmenchants.api;

import me.ashenguard.agmenchants.AGMEnchants;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;

public class Messenger {
    public static FileConfiguration config = null;
    public static JavaPlugin plugin = null;
    public static File exceptionFolder = null;

    public static void setup(FileConfiguration config, JavaPlugin plugin) {
        Messenger.config = config;
        Messenger.plugin = plugin;
        exceptionFolder = new File(plugin.getDataFolder(), "Exception");
        if (!exceptionFolder.exists())
            if (exceptionFolder.mkdirs())
                Messenger.Debug("General", "Exception folder wasn't found, A new one created");
    }

    private static boolean getBoolean(String path, boolean def) { return config == null ? def : config.getBoolean(path, def);}
    private static String getPrefix() {return config.getString("Prefix");}

    public static void Debug(String type, String message) {
        if (getBoolean("Debug.Enable", true) && getBoolean("Debug." + type, false)) {
            message = "§7[" + getPrefix() + "] §eDEBUG§r §6" + type + "§r " + message;
            Bukkit.getServer().getConsoleSender().sendMessage(message);
            sendInGame("Debug", message);
        }
    }

    public static void Debug(String type, String message, String... details) {
        if (getBoolean("Debug.Enable", true) && getBoolean("Debug." + type, false)) {
            message = "§7[" + getPrefix() + "] §eDEBUG§r §6" + type + "§r " + message;
            Bukkit.getServer().getConsoleSender().sendMessage(message);
            sendInGame("Debug", message);
            for (String detail : details) {
                detail = "              §7==>§r " + detail;
                Bukkit.getServer().getConsoleSender().sendMessage(detail);
                sendInGame("Debug", detail);
            }
        }
    }

    public static void Warning(String message) {
        message = "§7[" + getPrefix() + "] §cWARNING§r " + message;
        Bukkit.getServer().getConsoleSender().sendMessage(message);
        sendInGame("Warning", message);
    }

    public static void Info(String message) {
        message = "§7[" + getPrefix() + "] §aInfo§r " + message;
        Bukkit.getServer().getConsoleSender().sendMessage(message);
        sendInGame("Info", message);
    }

    public static void PlayerSend(Player player, String message) {
        message = "§7[§5" + getPrefix() + "§7]§r " + message;
        player.sendMessage(message);
    }
    public static void PlayerSend(CommandSender sender, String message) {
        message = "§7[§5" + getPrefix() + "§7]§r " + message;
        sender.sendMessage(message);
    }

    public static void sendInGame(String mode, String message) {
        if (!getBoolean("InGameMessages." + mode, true)) return;
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player:players)
            if (player.hasPermission("AGMEnchants.Messages." + mode))
                player.sendMessage(message);
    }

    public static void OPRemind(Player player) {
        if (!player.isOp()) return;

        BukkitScheduler scheduler = AGMEnchants.getInstance().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(AGMEnchants.getInstance(), () -> {
            // Update Check
            if (AGMEnchants.updateChecker.checkVersion()) {
                PlayerSend(player, "There is a §anew update§r available on SpigotMC");
                PlayerSend(player, "This version: §c" + AGMEnchants.getInstance().getDescription().getVersion() + "§r");
                PlayerSend(player, "SpigotMC version: §a" + AGMEnchants.updateChecker.getVersion() + "§r");
            }

            PlayerSend(player, "Check our website at: §bhttps://agmdev.xyz/§r");
            PlayerSend(player, "Join our discord server if you look for support: §6https://discord.gg/6exsySK");
            PlayerSend(player, "If you like this plugin, make sure you support us on §cPatreon§r: " +
                    "§bhttps://www.patreon.com/agmdevelopment/§r");
        }, 100L);
    }

    public static void ExceptionHandler(Exception exception) {
        ExceptionHandler(exception, exceptionFolder);
    }
    public static void ExceptionHandler(Exception exception, File exceptionFolder) {
        Messenger.Warning("An exception occurred");
        File file;
        int count = 0;
        do {
            count++;
            file = new File(exceptionFolder,"Exception_" + count + ".warn");
        } while (file.exists());
        try {
            PrintStream ps = new PrintStream(file);
            exception.printStackTrace(ps);
            ps.close();
            Messenger.Warning("Saved as \"§cException_ " + count + ".warn§r\"");
        } catch (FileNotFoundException ignored) {
            exception.printStackTrace();
        }
    }
}
