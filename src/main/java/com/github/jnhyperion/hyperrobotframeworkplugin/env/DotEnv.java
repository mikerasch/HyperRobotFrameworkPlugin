package com.github.jnhyperion.hyperrobotframeworkplugin.env;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DotEnv {
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private DotEnv() {

    }

    private static void loadProperties() {
        // Load all system properties first
        System.getProperties().forEach((key, value) -> properties.setProperty((String) key, (String) value));

        // Load from .env if it exists and is a regular file
        Path path = Paths.get(".env");
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return;
        }

        try {
            Files.readAllLines(path)
                    .stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        int index = line.indexOf('=');
                        if (index > 0) {
                            String key = line.substring(0, index).trim();
                            String value = line.substring(index + 1).trim();
                            properties.setProperty(key, value);
                        }
                    });
        } catch (IOException ignored) {

        }
    }

    public static String getValue(String key) {
        return properties.getProperty(key);
    }
}
