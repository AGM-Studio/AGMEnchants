package me.ashenguard.agmenchants.agmclasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class AGMException {
    public static void ExceptionHandler(Exception exception, File exceptionFolder) {
        AGMMessenger.Warning("An exception occurred");
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
            AGMMessenger.Warning("Saved as \"§cException_ " + count + ".warn§r\"");
        } catch (FileNotFoundException ignored) {
            exception.printStackTrace();
        }
    }
}
