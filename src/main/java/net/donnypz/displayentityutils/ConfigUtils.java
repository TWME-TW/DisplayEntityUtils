package net.donnypz.displayentityutils;

import net.donnypz.displayentityutils.managers.MYSQLManager;
import net.donnypz.displayentityutils.managers.MongoManager;
import net.donnypz.displayentityutils.utils.CullOption;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

final class ConfigUtils {
    
    private ConfigUtils(){}


    static void setConfigVariables(FileConfiguration config){
        DisplayEntityPlugin.isLocalEnabled = config.getBoolean("localStorageEnabled");
        if (config.getBoolean("mongodb.enabled")){
            DisplayEntityPlugin.isMongoEnabled = true;
            String cString = config.getString("mongodb.connectionString");
            String databaseName = config.getString("mongodb.database");
            String groupCollection = config.getString("mongodb.groupCollection");
            String animationCollection = config.getString("mongodb.animationCollection");
            MongoManager.createConnection(cString, databaseName, groupCollection, animationCollection);
        }

        if (config.getBoolean("mysql.enabled")){
            DisplayEntityPlugin.isMYSQLEnabled = true;
            String username = config.getString("mysql.username");
            String password = config.getString("mysql.password");
            if (!config.getString("mysql.connectionURL").isBlank()){
                MYSQLManager.createConnection(config.getString("mysql.connectionURL"), username, password);
            }
            else{
                String database = config.getString("mysql.database");
                String host = config.getString("mysql.host");
                int port = config.getInt("mysql.port");
                boolean useSSL = config.getBoolean("mysql.useSSL");
                MYSQLManager.createConnection(host, port, database, username, password, useSSL);
            }
        }

        DisplayEntityPlugin.seededPartUUIDs = config.getBoolean("seededPartUUIDs");

        DisplayEntityPlugin.automaticGroupDetection = config.getBoolean("automaticGroupDetection.enabled");
        if (DisplayEntityPlugin.automaticGroupDetection){
            DisplayEntityPlugin.maximumInteractionSearchRange = config.getDouble("automaticGroupDetection.maximumInteractionSearchRange");
            if (DisplayEntityPlugin.maximumInteractionSearchRange < 0){
                DisplayEntityPlugin.maximumInteractionSearchRange = 0;
            }
            DisplayEntityPlugin.readSameChunks = config.getBoolean("automaticGroupDetection.readSameChunks");
            DisplayEntityPlugin.unregisterOnUnload = config.getBoolean("automaticGroupDetection.unregisterOnUnload.enabled");
            DisplayEntityPlugin.isUnregisterOnUnloadBlacklist = config.getBoolean("automaticGroupDetection.unregisterOnUnload.blacklist");
            DisplayEntityPlugin.unregisterUnloadWorlds = config.getStringList("automaticGroupDetection.unregisterOnUnload.worlds");
        }

        DisplayEntityPlugin.autoPivotInteractions = config.getBoolean("autoPivotInteractionsOnSpawn");
        DisplayEntityPlugin.despawnGroupsOnServerStop = config.getBoolean("despawnGroupsOnServerStop");
        DisplayEntityPlugin.overwriteExistingSaves = config.getBoolean("overwriteExistingSaves");
        DisplayEntityPlugin.autoSelectGroups = config.getBoolean("autoSelectGroups");
        String cullConfig = config.getString("cullOption");
        try{
            if (cullConfig != null){
                DisplayEntityPlugin.cullOption = CullOption.valueOf(cullConfig.toUpperCase());
            }
            else{
                DisplayEntityPlugin.cullOption = CullOption.NONE;
            }
        }
        catch(IllegalArgumentException illegalArgumentException){
            DisplayEntityPlugin.cullOption = CullOption.NONE;
        }

    }
    
    
    static void updateConfig(){
        DisplayEntityPlugin plugin = DisplayEntityPlugin.getInstance();
        File configFile = new File(plugin.getDataFolder()+"/config.yml");
        YamlConfiguration externalConfig = YamlConfiguration.loadConfiguration(configFile);

        InputStreamReader defConfigStream = new InputStreamReader(plugin.getResource("config.yml"));
        YamlConfiguration resourceConfig = YamlConfiguration.loadConfiguration(defConfigStream);


        boolean wasUpdated = false;
        for (String string : resourceConfig.getKeys(true)) {
            if (!externalConfig.contains(string)) {
                externalConfig.set(string, resourceConfig.get(string));
                wasUpdated = true;
            }
        }

        try {
            externalConfig.save(configFile);
            if (wasUpdated){
                Bukkit.getConsoleSender().sendMessage(DisplayEntityPlugin.pluginPrefix+ ChatColor.YELLOW+" Plugin Config Updated!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
