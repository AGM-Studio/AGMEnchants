package me.ashenguard.agmenchants.remote;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.enchants.CustomEnchant;
import me.ashenguard.agmenchants.enchants.Enchant;
import me.ashenguard.agmenchants.managers.EnchantManager;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.utils.WebReader;
import me.ashenguard.api.versions.Version;

import java.util.ArrayList;
import java.util.List;

public class RemoteEnchant {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private static final Messenger MESSENGER = AGMEnchants.getMessenger();
    private static final EnchantManager ENCHANT_MANAGER = AGMEnchants.getEnchantManager();

    private static final String REMOTE_LINK = "https://raw.githubusercontent.com/Ashengaurd/AGMEnchants/master/Enchants.md";

    public final String id;
    public final Version current;
    public final Version version;
    public final boolean official;
    public final Status status;

    private static List<RemoteEnchant> fetchRemoteEnchantments() {
        List<RemoteEnchant> found = new ArrayList<>();
        List<String> lines = new WebReader(REMOTE_LINK).readLines();

        int splitIndex = lines.indexOf("# Unofficial Enchantments");

        for (String line: lines) {
            if (!line.startsWith("* ")) continue;
            line = line.replace("* ", "");
            String id = line.substring(0, line.indexOf("-"));
            String version = line.substring(line.indexOf("-") + 1).replace(" ", "");

            found.add(new RemoteEnchant(id, new Version(version), splitIndex > lines.indexOf(line)));
        }

        return found;
    }
    public static List<RemoteEnchant> fetchAvailableRemoteEnchants() {
        List<RemoteEnchant> enchantments = new ArrayList<>();
        List<RemoteEnchant> found = RemoteEnchant.fetchRemoteEnchantments();

        for (RemoteEnchant enchantment: found) {
            switch (enchantment.status) {
                case BLACKLISTED:
                case INSTALLED:
                    break;
                case NOT_INSTALLED:
                    enchantments.add(enchantment);
                    MESSENGER.Debug("Update", "A new enchantment found on page", "Name= §6" + enchantment.id, "Version= §6" + enchantment.version, "Official= §6" + (enchantment.official ? "Yes" : "No"));
                    break;
                case UPDATE_AVAILABLE:
                    enchantments.add(enchantment);
                    MESSENGER.Debug("Update", "An update was found on page for an enchantment", "Name= §6" + enchantment.id, "Version= §6" + enchantment.version, "Current= §6" + enchantment.current);
                    break;
            }
        }

        return enchantments;
    }

    public RemoteEnchant(String id, Version version, boolean official) {
        this.id = id;
        this.version = version;
        this.official = official;

        List<String> blacklist = PLUGIN.getConfig().getStringList("Check.BlacklistedEnchants");
        Enchant temp = ENCHANT_MANAGER.STORAGE.get(id);
        if (temp instanceof CustomEnchant) {
            CustomEnchant enchantment = (CustomEnchant) temp;
            if (blacklist.contains(id)) status = Status.BLACKLISTED;
            else if (version.isHigher(enchantment.getVersion())) status = Status.UPDATE_AVAILABLE;
            else status = Status.INSTALLED;

            current = enchantment.getVersion();
        } else {
            status = temp == null ? Status.NOT_INSTALLED : Status.INSTALLED;
            current = new Version(0, 0);
        }
    }

    public enum Status {
        BLACKLISTED, NOT_INSTALLED, UPDATE_AVAILABLE, INSTALLED
    }
}
