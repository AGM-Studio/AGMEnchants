package me.ashenguard.agmenchants.agmclasses;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.classes.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;

public class AGMMessenger {
    private static FileConfiguration config = AGMEnchants.config;
    private static String prefix = config.getString("Prefix");

    public static void Debug(String type, String message) {
        if (!config.getBoolean("Debug.Enable" )) return;
        if (!config.getBoolean("Debug." + type)) return;
        message = "§7[" + prefix + "] §eDEBUG§r §6" + type + "§r " + message;
        Bukkit.getServer().getConsoleSender().sendMessage(message);
        sendInGame("Debug", message);
    }

    public static void Debug(String type, String message, String... details) {
        if (!config.getBoolean("Debug.Enable" )) return;
        if (!config.getBoolean("Debug." + type)) return;
        message = "§7[" + prefix + "] §eDEBUG§r §6" + type + "§r " + message;
        Bukkit.getServer().getConsoleSender().sendMessage(message);
        sendInGame("Debug", message);
        for (String detail:details) {
            detail = "              §7==>§r " + detail;
            Bukkit.getServer().getConsoleSender().sendMessage(detail);
            sendInGame("Debug", detail);
        }
    }

    public static void Warning(String message) {
        message = "§7[" + prefix + "] §cWARNING§r " + message;
        Bukkit.getServer().getConsoleSender().sendMessage(message);
        sendInGame("Warning", message);
    }

    public static void Info(String message) {
        message = "§7[" + prefix + "] §aInfo§r " + message;
        Bukkit.getServer().getConsoleSender().sendMessage(message);
        sendInGame("Info", message);
    }

    public static void PlayerSend(Player player, String message) {
        message = "§7[§5" + prefix + "§7]§r " + message;
        player.sendMessage(message);
    }

    public static void PlayerSend(CommandSender sender, String message) {
        message = "§7[§5" + prefix + "§7]§r " + message;
        sender.sendMessage(message);
    }

    public static void sendInGame(String mode, String message) {
        if (!config.getBoolean("InGameMessages." + mode)) return;
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
            new UpdateChecker(AGMEnchants.getInstance(), 75787).getVersion(version -> {
                if (!AGMEnchants.getInstance().getDescription().getVersion().equalsIgnoreCase(version)) {
                    PlayerSend(player, "There is a §anew update§r available: " +
                            "§bhttps://www.spigotmc.org/§r");
                    PlayerSend(player, "This version: §c" + AGMEnchants.getInstance().getDescription().getVersion() + "§r");
                    PlayerSend(player, "Spigotmc version: §a" + version + "§r");
                }
            });

            // Patreon page
            PlayerSend(player, "Check our website at: §bhttps://agmdev.xyz/§r");
            PlayerSend(player, "Join our discord server if you look for support: §6https://discord.gg/6exsySK");
            PlayerSend(player, "If you like this plugin, make sure you support us on §cPatreon§r: " +
                    "§bhttps://www.patreon.com/agmdevelopment/§r");
        }, 100L);
    }
}
