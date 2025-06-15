package com.github.jnhyperion.hyperrobotframeworkplugin.env;

import com.github.jnhyperion.hyperrobotframeworkplugin.ide.config.RobotOptionsProvider;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

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
            logProperties();
        } catch (IOException ignored) {

        }
    }

    private static void logProperties() {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        String lookup = "PropertiesContents";
        String data = properties.toString();
        if (RobotOptionsProvider.getInstance(project).isDebug()) {
            String message = String.format("[DotEnv][%s] %s", lookup, data);
            Notifications.Bus.notify(new Notification("intellibot.debug", "Debug", message, NotificationType.INFORMATION));
        }
    }

    public static String getValue(String key) {
        return properties.getProperty(key);
    }
}
