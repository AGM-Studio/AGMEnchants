package me.ashenguard.agmenchants;

import me.ashenguard.api.versions.Version;
import me.ashenguard.spigotapplication.SpigotPanel;

public class Panel extends SpigotPanel {
    public Panel() {
        super(81800, new Version(4, 0));
        this.addDependency(83245);  // AGMCore
        this.addDependency(6245);   // Placeholder API
        this.setDescription("Installation:\n" +
                "Add the JAR file to your server plugins folder and reload the server.\n\n" +
                "About this plugin:\n" +
                "This plugin is a placeholder where you can install enchantments and runes to add an amazing experience\n" +
                "You can find enchantments and runes at our spigot page in a separate resource page.\n" +
                "\n" +
                "Install enchantments/runes:\n" +
                "Drag the JAR file to Enchants/Runes folder placed in the plugin folder and reload the server.");
    }

    public static void main(String[] args) {
        launch();
    }
}
