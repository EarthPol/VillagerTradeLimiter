package com.pretzel.dev.villagertradelimiter.lib;

import com.pretzel.dev.villagertradelimiter.VillagerTradeLimiter;
import org.bukkit.plugin.java.JavaPlugin;

public class Debug {
    private static VillagerTradeLimiter plugin;
    private static boolean debugEnabled;

    // Initialize the Debug class with the plugin instance and debug config setting
    public static void initialize(VillagerTradeLimiter pluginInstance) {
        plugin = pluginInstance;
        debugEnabled = plugin.getCfg().getBoolean("debug", false);
    }

    // Log a debug message if debug is enabled
    public static void log(String message) {
        if (debugEnabled) {
            plugin.getLogger().info("[DEBUG] " + message);  // Prefix "[DEBUG]" to easily identify debug logs
        }
    }
}

