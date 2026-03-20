package com.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigurationReader {

    private static final Properties CONFIG_FILE = loadProperties();

    private ConfigurationReader() {
    }

    public static String getProperty(String key) {
        return CONFIG_FILE.getProperty(key);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path configPath = Path.of("config.properties");

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load configuration from " + configPath.toAbsolutePath(), exception);
        }
    }
}
