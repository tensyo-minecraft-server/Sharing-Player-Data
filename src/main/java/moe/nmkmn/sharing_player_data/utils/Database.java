package moe.nmkmn.sharing_player_data.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public record Database(Connection connection) {
    public void initialize() throws SQLException {
        Statement statement = connection().createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS data_money (uuid varchar(36) primary key, balance double)");
        statement.close();
    }
}
