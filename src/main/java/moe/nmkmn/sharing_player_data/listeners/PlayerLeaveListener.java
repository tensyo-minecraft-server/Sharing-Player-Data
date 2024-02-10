package moe.nmkmn.sharing_player_data.listeners;

import moe.nmkmn.sharing_player_data.Main;
import moe.nmkmn.sharing_player_data.models.MoneyModel;
import moe.nmkmn.sharing_player_data.utils.MoneyDB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.OptionalDouble;

public class PlayerLeaveListener implements Listener {
    private final Main plugin;

    public PlayerLeaveListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            Player player = event.getPlayer();
            Connection connection;

            MoneyDB moneyDB = new MoneyDB();
            MoneyModel moneyModel;

            try {
                connection = plugin.getDataSource().getConnection();
                moneyModel = moneyDB.get(connection, player.getUniqueId().toString());
            } catch (SQLException e) {
                plugin.getLogger().severe(e.getMessage());
                return;
            }

            try {
                OptionalDouble value = plugin.getJecon().getRepository().getDouble(player.getUniqueId());

                if (value.isPresent()) {
                    if (moneyModel == null) {
                        moneyDB.create(connection, new MoneyModel(player.getUniqueId().toString(), value.getAsDouble()));
                    } else  {
                        moneyDB.update(connection, new MoneyModel(player.getUniqueId().toString(), value.getAsDouble()));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe(e.getMessage());
            }

            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 2L);
    }
}
