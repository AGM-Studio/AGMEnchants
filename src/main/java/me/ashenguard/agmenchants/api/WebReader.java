package me.ashenguard.agmenchants.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class WebReader {
    public static List<String> readLines(String url) {
        List<String> result = new ArrayList<>();

        try {
            URLConnection con = new URL(url).openConnection();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while ((line = buffer.readLine()) != null)
                result.add(line);

        } catch (IOException ignored) {}

        return result;
    }

    public static String read(String url) {
        List<String> lines = readLines(url);
        StringBuilder result = new StringBuilder();
        for (String line: lines) {
            result.append(line.replace("\n", "")).append("\n");
        }

        return result.toString();
    }
}
