package moe.nmkmn.sharing_player_data.listeners;

import moe.nmkmn.sharing_player_data.Main;
import moe.nmkmn.sharing_player_data.models.MoneyModel;
import moe.nmkmn.sharing_player_data.utils.MoneyDB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.SQLException;

public class PlayerJoinListener implements Listener {
    private final Main plugin;

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            Connection connection;
            MoneyModel moneyModel;

            try {
                connection = plugin.getDataSource().getConnection();
            } catch (SQLException e) {
                player.sendMessage("[SPD] データベースの接続に失敗しました。");
                return;
            }

            try {
                MoneyDB moneyDB = new MoneyDB();
                moneyModel = moneyDB.getUUIDByDatabase(connection, String.valueOf(player.getUniqueId()), 10000.0);
            } catch (SQLException e) {
                moneyModel = new MoneyModel(player.getUniqueId().toString(), 10000.0);
                player.sendMessage("[SPD] 所持金を取得できませんでした、初期の所持金ににします。");
            }

            try {
                plugin.getJecon().getRepository().set(event.getPlayer().getUniqueId(), moneyModel.getBalance());
                player.sendMessage("[SPD] 所持金を同期しました！");
            } catch (Exception e) {
                player.sendMessage("[SPD] 正常に所持金を同期できませんでした。\nエラー: " + e.getMessage());
            }

            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 5L);
    }
}
