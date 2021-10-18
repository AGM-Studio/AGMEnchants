package me.ashenguard.agmenchants.remote;

import me.ashenguard.agmenchants.AGMEnchants;
import me.ashenguard.agmenchants.managers.RuneManager;
import me.ashenguard.agmenchants.runes.Rune;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.utils.WebReader;
import me.ashenguard.api.versions.Version;

import java.util.ArrayList;
import java.util.List;

public class RemoteRune {
    private static final AGMEnchants PLUGIN = AGMEnchants.getInstance();
    private static final Messenger MESSENGER = AGMEnchants.getMessenger();

    private static final String REMOTE_LINK = "https://raw.githubusercontent.com/Ashengaurd/AGMEnchants/master/Runes.md";

    public final String id;
    public final Version current;
    public final Version version;
    public final boolean official;
    public final Status status;

    private static List<RemoteRune> fetchRemoteRunes() {
        List<RemoteRune> found = new ArrayList<>();
        List<String> lines = new WebReader(REMOTE_LINK).readLines();

        int splitIndex = lines.indexOf("# Unofficial Runes");

        for (String line: lines) {
            if (!line.startsWith("* ")) continue;
            line = line.replace("* ", "");
            String id = line.substring(0, line.indexOf("-"));
            String version = line.substring(line.indexOf("-") + 1).replace(" ", "");

            found.add(new RemoteRune(id, new Version(version), splitIndex > lines.indexOf(line)));
        }

        return found;
    }
    public static List<RemoteRune> fetchAvailableRemoteRunes() {
        List<RemoteRune> enchantments = new ArrayList<>();
        List<RemoteRune> found = RemoteRune.fetchRemoteRunes();

        for (RemoteRune rune: found) {
            switch (rune.status) {
                case BLACKLISTED:
                case INSTALLED:
                    break;
                case NOT_INSTALLED:
                    enchantments.add(rune);
                    MESSENGER.Debug("Update", "A new rune was found on page", "Name= §6" + rune.id, "Version= §6" + rune.version, "Official= §6" + (rune.official ? "Yes" : "No"));
                    break;
                case UPDATE_AVAILABLE:
                    enchantments.add(rune);
                    MESSENGER.Debug("Update", "An update was found on page for an rune", "Name= §6" + rune.id, "Version= §6" + rune.version, "Current= §6" + rune.current);
                    break;
            }
        }

        return enchantments;
    }

    public RemoteRune(String id, Version version, boolean official) {
        this.id = id;
        this.version = version;
        this.official = official;

        List<String> blacklist = PLUGIN.getConfig().getStringList("Check.BlacklistedRunes");
        Rune temp = RuneManager.STORAGE.get(id);
        if (temp != null) {
            if (blacklist.contains(id)) status = Status.BLACKLISTED;
            else if (version.isHigher(temp.getVersion())) status = Status.UPDATE_AVAILABLE;
            else status = Status.INSTALLED;

            current = temp.getVersion();
        } else {
            status = Status.NOT_INSTALLED;
            current = new Version(0, 0);
        }
    }

    public enum Status {
        BLACKLISTED, NOT_INSTALLED, UPDATE_AVAILABLE, INSTALLED
    }
}
