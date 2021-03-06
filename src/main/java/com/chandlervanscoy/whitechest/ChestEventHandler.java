package com.chandlervanscoy.whitechest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ChestEventHandler implements Listener {
    @EventHandler
    public void handleBlockPlace(BlockPlaceEvent event) {
        if(WhiteChest.config.getBoolean("debug")) {
            event.getPlayer().sendMessage("Block Namespaced Key: " + WhiteChest.getNamespaceKeyString(event.getBlockPlaced().getType().getKey()));
        }

        String blockKey = WhiteChest.getNamespaceKeyString(event.getBlockPlaced().getType().getKey());
        List<String> blockTypes = WhiteChest.config.getStringList("blockList");


        if(WhiteChest.config.getBoolean("debug")) {
            WhiteChest.getPlugin().getLogger().info("Block Key: " + blockKey);
            WhiteChest.getPlugin().getLogger().info("Block List: " + String.join(", ", blockTypes));
        }

        if(!blockTypes.contains(blockKey)) return;

        try {
            PreparedStatement statement = WhiteChest.connection.prepareStatement("INSERT INTO chests (player, locationX, locationY, locationZ) VALUES (?, ?, ?, ?)");
            statement.setString(1, event.getPlayer().getUniqueId().toString());
            statement.setInt(2, event.getBlockPlaced().getX());
            statement.setInt(3, event.getBlockPlaced().getY());
            statement.setInt(4, event.getBlockPlaced().getZ());

            int rowsAffected = statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @EventHandler
    public void handleBlockBreak(BlockBreakEvent event) {
        if(!event.getBlock().getType().getKey().getKey().equals("chest")) return;

        try {
            PreparedStatement statement = WhiteChest.connection.prepareStatement("DELETE FROM chests WHERE locationX = ? AND locationY = ? AND locationZ = ?");
            statement.setInt(1, event.getBlock().getX());
            statement.setInt(2, event.getBlock().getY());
            statement.setInt(3, event.getBlock().getZ());

            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @EventHandler
    public void handleChestInteraction(PlayerInteractEvent event) {
        if(!event.hasBlock()) return;
        if(!WhiteChest.config.getBoolean("actionOnBlockBreak") && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        String blockKey = WhiteChest.getNamespaceKeyString(event.getClickedBlock().getType().getKey());
        List<String> blockTypes = WhiteChest.config.getStringList("blockList");
        if(!blockTypes.contains(blockKey)) return;

        UUID chestOwner = null;
        try {
            PreparedStatement statement = WhiteChest.connection.prepareStatement("SELECT * FROM chests WHERE locationX = ? AND locationY = ? AND locationZ = ? LIMIT 1");
            statement.setInt(1, event.getClickedBlock().getX());
            statement.setInt(2, event.getClickedBlock().getY());
            statement.setInt(3, event.getClickedBlock().getZ());

            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                chestOwner = UUID.fromString(rs.getString("player"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if(chestOwner == null) return;
        if(chestOwner.toString().equals(event.getPlayer().getUniqueId().toString())) return;

        boolean foundPlayer = false;
        try {
            PreparedStatement statement = WhiteChest.connection.prepareStatement("SELECT * FROM whitelist WHERE owner = ?");
            statement.setString(1, chestOwner.toString());

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                if(event.getPlayer().getUniqueId().toString().equals(rs.getString("player"))) foundPlayer = true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if(foundPlayer) return;

        event.getPlayer().sendMessage("You do not own this chest! As such, prepare to die.");

        String deathType = WhiteChest.config.getString("deathType");

        if(deathType == null) {
            event.getPlayer().damage(200);
            return;
        }

        switch (deathType) {
            case "falldamage":
                event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), event.getPlayer().getLocation().getX(), 255, event.getPlayer().getLocation().getZ()));
                break;
            case "instant":
            default:
                event.getPlayer().damage(200);
                break;
        }

    }
}
