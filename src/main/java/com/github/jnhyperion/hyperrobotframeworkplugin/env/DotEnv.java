package com.github.jnhyperion.hyperrobotframeworkplugin.env;

import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DotEnv {
    private static final Map<Project, Properties> projectToPropertiesMap = new ConcurrentHashMap<>();

    private DotEnv() {

    }

    private static void loadProperties(Project project) {
        if (projectToPropertiesMap.containsKey(project)) {
            return;
        }
        Properties properties = new Properties();
        // Load all system properties first
        System.getProperties().forEach((key, value) -> properties.setProperty((String) key, (String) value));

        String basePath = project.getBasePath();

        // Load from .env if it exists and is a regular file
        Path path = Paths.get(basePath + ".env");
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            projectToPropertiesMap.put(project, properties);
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
        projectToPropertiesMap.put(project, properties);
    }

    public static String getValue(String key, Project project) {
        loadProperties(project);
        return projectToPropertiesMap.get(project).getProperty(key);
    }
}
