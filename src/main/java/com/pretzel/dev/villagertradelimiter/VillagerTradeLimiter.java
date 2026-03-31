package com.pretzel.dev.villagertradelimiter;

import com.pretzel.dev.villagertradelimiter.commands.CommandManager;
import com.pretzel.dev.villagertradelimiter.commands.CommandBase;
import com.pretzel.dev.villagertradelimiter.data.PlayerData;
import com.pretzel.dev.villagertradelimiter.database.DatabaseManager;
import com.pretzel.dev.villagertradelimiter.lib.Debug;
import com.pretzel.dev.villagertradelimiter.listeners.InventoryListener;
import com.pretzel.dev.villagertradelimiter.listeners.VillagerListener;
import com.pretzel.dev.villagertradelimiter.settings.ConfigUpdater;
import com.pretzel.dev.villagertradelimiter.lib.Util;
import com.pretzel.dev.villagertradelimiter.listeners.PlayerListener;
import com.pretzel.dev.villagertradelimiter.settings.Lang;
import com.pretzel.dev.villagertradelimiter.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VillagerTradeLimiter extends JavaPlugin {
    public static final String PLUGIN_NAME = "VillagerTradeLimiter";
    public static final String PREFIX = ChatColor.GOLD+"["+PLUGIN_NAME+"] ";

    //Settings
    private volatile FileConfiguration cfg;
    private volatile Lang lang;
    private CommandManager commandManager;
    private DatabaseManager databaseManager;
    private PlayerListener playerListener;
    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    /** Initial plugin load/unload */
    public void onEnable() {
        //Initialize instance variables
        this.cfg = null;
        this.commandManager = new CommandManager(this);

        //Copy default settings & load settings
        this.getConfig().options().copyDefaults();
        this.saveDefaultConfig();
        this.loadSettings();

        //Register commands and listeners
        this.registerCommands();
        this.registerListeners();

        //Initialize debug logging
        Debug.initialize(this);

        //Send enabled message
        Util.consoleMsg(PREFIX+PLUGIN_NAME+" is running!");
    }

    /** Save database on plugin stop, server stop */
    public void onDisable() {
        if (this.databaseManager == null) {
            this.playerData.clear();
            return;
        }

        for(UUID uuid : playerData.keySet()) {
            this.databaseManager.savePlayer(uuid, false);
        }
        this.playerData.clear();
    }

    /** Loads or reloads config.yml and messages.yml */
    public void loadSettings() {
        final String mainPath = this.getDataFolder().getPath()+"/";
        final File file = new File(mainPath, "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", file, Collections.singletonList("Overrides"));
        } catch (IOException e) {
            Util.errorMsg(e);
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
        this.lang = new Lang(this, this.getTextResource("messages.yml"), mainPath);

        //Load/reload database manager
        if(this.databaseManager == null) this.databaseManager = new DatabaseManager(this);
        else onDisable();
        this.databaseManager.load();
        Debug.initialize(this);
    }

    /** Registers plugin commands */
    private void registerCommands() {
        final CommandBase cmd = this.commandManager.getCommands();
        this.getCommand("villagertradelimiter").setExecutor(cmd);
        this.getCommand("villagertradelimiter").setTabCompleter(cmd);
    }

    /** Registers plugin listeners */
    private void registerListeners() {
        final Settings settings = new Settings(this);
        this.playerListener = new PlayerListener(this, settings);
        this.getServer().getPluginManager().registerEvents(this.playerListener, this);
        this.getServer().getPluginManager().registerEvents(new InventoryListener(this, settings), this);
        this.getServer().getPluginManager().registerEvents(new VillagerListener(this, settings), this);
    }


    // ------------------------- Getters -------------------------
    /** @return the settings from config.yml */
    public FileConfiguration getCfg() { return this.cfg; }

    /** @param path the key you want the value for
      * @return a language setting from messages.yml */
    public String getLang(final String path) { return this.lang.get(path); }

    /** @return this plugin's player listener */
    public PlayerListener getPlayerListener() { return this.playerListener; }

    /** @return a player's data container */
    public Map<UUID, PlayerData> getPlayerData() { return this.playerData; }

    /** @return the invsee inventory's barrier block */
    public ItemStack getBarrier() { return this.commandManager.getBarrier(); }
}
