package me.ashenguard.agmenchants.enchants.remote;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.EnchantmentManager;
import me.ashenguard.agmenchants.enchants.custom.CustomEnchantment;
import me.ashenguard.api.WebReader;
import me.ashenguard.api.utils.Version;

import java.util.ArrayList;
import java.util.List;

public class RemoteEnchantment {
    public final static String REMOTE = "https://raw.githubusercontent.com/Ashengaurd/AGMEnchants/master/Enchantments.md";

    public final String id;
    public final Version current;
    public final Version version;
    public final boolean official;
    public final RemoteEnchantmentStatus status;

    public static List<RemoteEnchantment> getAllEnchantments() {
        List<RemoteEnchantment> found = new ArrayList<>();
        List<String> lines = new WebReader(REMOTE).readLines();

        int splitIndex = lines.indexOf("# Unofficial Enchantments");

        for (String line: lines) {
            if (!line.startsWith("* ")) continue;
            line = line.replace("* ", "");
            String id = line.substring(0, line.indexOf("-"));
            String version = line.substring(line.indexOf("-") + 1).replace(" ", "");

            found.add(new RemoteEnchantment(id, new Version(version), splitIndex > lines.indexOf(line)));
        }

        return found;
    }
    public static List<RemoteEnchantment> getRemoteEnchantments() {
        List<RemoteEnchantment> enchantments = new ArrayList<>();
        List<RemoteEnchantment> found = RemoteEnchantment.getAllEnchantments();

        for (RemoteEnchantment enchantment: found) {
            switch (enchantment.status) {
                case BLACKLISTED:
                case INSTALLED:
                    break;
                case NOT_INSTALLED:
                    enchantments.add(enchantment);
                    AGMEnchants.Messenger.Debug("Update", "A new enchantment found on page", "Name= §6" + enchantment.id, "Version= §6" + enchantment.version, "Official= §6" + (enchantment.official ? "Yes" : "No"));
                    break;
                case UPDATE_AVAILABLE:
                    enchantments.add(enchantment);
                    AGMEnchants.Messenger.Debug("Update", "An update was found on page for an enchantment", "Name= §6" + enchantment.id, "Version= §6" + enchantment.version, "Current= §6" + enchantment.current);
                    break;
            }
        }

        return enchantments;
    }

    public RemoteEnchantment(String id, Version version, boolean official) {
        this.id = id;
        this.version = version;
        this.official = official;

        List<String> blacklist = AGMEnchants.config.getStringList("Check.BlacklistedEnchantments");
        CustomEnchantment enchantment = EnchantmentManager.getCustomEnchantment(id);

        if (blacklist.contains(id)) status = RemoteEnchantmentStatus.BLACKLISTED;
        else if (enchantment == null) status = RemoteEnchantmentStatus.NOT_INSTALLED;
        else if (enchantment.getVersion().isLower(version)) status = RemoteEnchantmentStatus.UPDATE_AVAILABLE;
        else status = RemoteEnchantmentStatus.INSTALLED;

        current = enchantment != null ? enchantment.getVersion() : new Version(0, 0);

        AGMEnchants.Messenger.Debug("Update", "An enchantment was found on remote server", "Enchantment= §6" + id, "Version= §6" + version);
    }
}
