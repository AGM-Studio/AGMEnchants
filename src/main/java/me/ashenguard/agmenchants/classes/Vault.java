package me.ashenguard.agmenchants.classes;


import me.ashenguard.agmenchants.agmclasses.AGMMessenger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class Vault {

    public boolean enable;
    public Permission permission = null;
    public Economy economy = null;
    public Chat chat = null;

    public Vault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            enable = false;
            AGMMessenger.Warning("§6Vault§r wasn't found");
            return;
        }

        boolean enableA = setupEconomy();
        AGMMessenger.Debug("Vault", "Vault economy is " + (enableA? "§aenable": "§cdisable"));
        boolean enableB = setupChat();
        AGMMessenger.Debug("Vault", "Vault chat is " + (enableB? "§aenable": "§cdisable"));
        boolean enableC = setupPermissions();
        AGMMessenger.Debug("Vault", "Vault permission is " + (enableC? "§aenable": "§cdisable"));

        enable = enableA && enableC;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return false;
        chat = rsp.getProvider();
        return chat != null;
    }
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        permission = rsp.getProvider();
        return permission != null;
    }

    public void withdrawPlayerMoney(Player player, double amount) {
        economy.withdrawPlayer(player, amount);
        AGMMessenger.Debug("Vault", "Player withdraw some money", "Player= §6" + player.getName(), "Money= §6" + amount, "New Balance= §6" + (int) economy.getBalance(player));
    }

    public void depositPlayerMoney(Player player, double amount) {
        economy.depositPlayer(player, amount);
        AGMMessenger.Debug("Vault", "Player deposit some money", "Player= §6" + player.getName(), "Money= §6" + amount, "New Balance= §6" + (int) economy.getBalance(player));
    }
}
