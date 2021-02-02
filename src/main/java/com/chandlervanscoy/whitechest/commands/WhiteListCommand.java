package com.chandlervanscoy.whitechest.commands;

import com.chandlervanscoy.whitechest.WhiteChest;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class WhiteListCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            String type = args[0];

            if(type.equals("list")) {
                try {
                    PreparedStatement statement = WhiteChest.connection.prepareStatement("SELECT * FROM whitelist WHERE owner = ?");
                    statement.setString(1, player.getUniqueId().toString());

                    ResultSet rs = statement.executeQuery();
                    while (rs.next()) {
                        UUID playerId = UUID.fromString(rs.getString("player"));
                        Player foundPlayer = Bukkit.getPlayer(playerId);
                        if(foundPlayer != null) {
                            player.sendMessage(foundPlayer.getDisplayName());
                        } else {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                            String name = offlinePlayer.getName();
                            if(name != null) player.sendMessage(name);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    return false;
                }
                return true;
            }

            if(args.length != 2) {
                return false;
            }

            String playerName = args[1];
            PreparedStatement statement = null;

            Player newPlayer = Bukkit.getPlayer(playerName);
            if(newPlayer == null) return false;

            try {
                if(type.equals("add")) {
                    statement = WhiteChest.connection.prepareStatement("INSERT INTO whitelist (owner, player) VALUES (?, ?)");
                } else if (type.equals("remove")) {
                    statement = WhiteChest.connection.prepareStatement("DELETE FROM whitelist WHERE owner = ? AND player = ?");
                }
                assert statement != null;
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, newPlayer.getUniqueId().toString());
                statement.executeUpdate();

                switch (type) {
                    case "add":
                        player.sendMessage("Added " + newPlayer.getDisplayName() + " to your chest whitelist.");
                        break;
                    case "remove":
                        player.sendMessage("Removed " + newPlayer.getDisplayName() + " to your chest whitelist.");
                        break;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                return false;
            }
        }

        return true;
    }
}
