package com.ezlevup.util;

import java.io.*;
import java.util.Properties;

public class Settings {
    private static final String SETTINGS_FILE = "settings.properties";
    private static final String API_URL_KEY = "api.url";
    private static final String DEFAULT_API_URL = "http://localhost:8580";

    private static Properties properties = new Properties();

    static {
        loadSettings();
    }

    private static void loadSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILE);
            if (settingsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(settingsFile)) {
                    properties.load(fis);
                }
            } else {
                setDefaultSettings();
            }
        } catch (IOException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
            setDefaultSettings();
        }
    }

    private static void setDefaultSettings() {
        properties.setProperty(API_URL_KEY, DEFAULT_API_URL);
        saveSettings();
    }

    private static void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, "EzLevUp Application Settings");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    public static String getApiUrl() {
        return properties.getProperty(API_URL_KEY, DEFAULT_API_URL);
    }

    public static void setApiUrl(String apiUrl) {
        properties.setProperty(API_URL_KEY, apiUrl);
        saveSettings();
    }

    public static String getDefaultApiUrl() {
        return DEFAULT_API_URL;
    }

    public static String getLoginApiUrl() {
        return getApiUrl() + "/auth/login";
    }

    public static String getLogoutApiUrl() {
        return getApiUrl() + "/auth/logout";
    }
}