package moe.nmkmn.sharing_player_data.commands;

import moe.nmkmn.sharing_player_data.Main;
import moe.nmkmn.sharing_player_data.models.MoneyModel;
import moe.nmkmn.sharing_player_data.utils.MoneyDB;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.OptionalDouble;

public class SPDCommand implements CommandExecutor {
    public Main plugin;

    public SPDCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spd")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                sender.sendMessage("/spd initialize");
            } else {
                if (args[0].equalsIgnoreCase("initialize")) {
                    Connection connection;
                    try {
                        connection = plugin.getDataSource().getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    if (connection != null) {
                        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                            OptionalDouble value = plugin.getJecon().getRepository().getDouble(player.getUniqueId());

                            if (value.isPresent()) {
                                try {
                                    MoneyDB moneyDB = new MoneyDB();
                                    MoneyModel moneyModel = moneyDB.get(connection, String.valueOf(player.getUniqueId()));

                                    if (moneyModel == null) {
                                        moneyDB.create(connection, new MoneyModel(player.getUniqueId().toString(), value.getAsDouble()));
                                    } else {
                                        moneyDB.update(connection, new MoneyModel(player.getUniqueId().toString(), value.getAsDouble()));
                                    }

                                    sender.sendMessage("[SPD] Database Committed (" + player.getName() + ")");
                                } catch (SQLException e) {
                                    sender.sendMessage(e.getMessage());
                                }
                            }
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }
}
