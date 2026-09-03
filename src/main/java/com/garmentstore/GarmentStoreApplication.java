package com.garmentstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@EnableAsync
public class GarmentStoreApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(GarmentStoreApplication.class, args);
    }

    /**
     * Automatically loads key-value pairs from a local .env file into System properties
     * before Spring starts, unless they are already present as System properties or OS env vars.
     */
    private static void loadDotEnv() {
        Path envPath = Paths.get(".env");
        if (!Files.exists(envPath)) {
            envPath = Paths.get(System.getProperty("user.dir", "."), ".env");
        }
        if (!Files.exists(envPath)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(envPath);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int separatorIdx = trimmed.indexOf('=');
                String key = trimmed.substring(0, separatorIdx).trim();
                String value = trimmed.substring(separatorIdx + 1).trim();

                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    if (value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }
                }

                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
            // If .env cannot be read, continue with standard environment and properties
        }
    }
}

