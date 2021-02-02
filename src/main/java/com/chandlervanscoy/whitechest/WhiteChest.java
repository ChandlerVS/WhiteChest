package com.chandlervanscoy.whitechest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public final class WhiteChest extends JavaPlugin {
    public static Configuration config;
    public static Connection connection;

    @Override
    public void onEnable() {
        config = getConfig();
        config.addDefault("debug", false);
        config.options().copyDefaults(true);
        saveConfig();

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDataFolder().getAbsolutePath() + "/data.db");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        createChestsTable();
        createWhiteListTable();

        getServer().getPluginManager().registerEvents(new ChestEventHandler(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void createWhiteListTable() {
        // Create the whitelist table if it doesnt exist
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name = 'whitelist'");
            if(!rs.next()) {
                Statement newWhiteListStatement = connection.createStatement();
                newWhiteListStatement.execute("create table whitelist( id integer constraint whitelist_pk primary key autoincrement, owner text not null, player text not null);");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void createChestsTable() {
        // Create the chests table if it doesn't exist
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name = 'chests'");

            if(!rs.next()) {
                Statement newChestTableStatement = connection.createStatement();
                statement.execute("create table chests( id integer constraint chests_pk primary key autoincrement, player text not null, locationX integer not null, locationY integer not null, locationZ integer not null);");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
}
