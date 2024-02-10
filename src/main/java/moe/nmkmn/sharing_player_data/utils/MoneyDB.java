package moe.nmkmn.sharing_player_data.utils;

import moe.nmkmn.sharing_player_data.models.MoneyModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MoneyDB {
    public void create(Connection connection, MoneyModel model) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO data_money(uuid, balance) VALUES (?, ?)"
        );

        statement.setString(1, model.getUUID());
        statement.setDouble(2, model.getBalance());

        statement.executeUpdate();
        connection.commit();
        statement.close();
    }

    public void update(Connection connection, MoneyModel model) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE data_money SET balance = ? WHERE uuid = ?"
        );

        statement.setDouble(1, model.getBalance());
        statement.setString(2, model.getUUID());

        statement.executeUpdate();
        connection.commit();
        statement.close();
    }

    public MoneyModel get(Connection connection, String uuid) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM data_money WHERE uuid = ?");

        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();
        MoneyModel moneyModel;

        if (resultSet.next()) {
            moneyModel = new MoneyModel(resultSet.getString("uuid"), resultSet.getDouble("balance"));

            statement.close();

            return moneyModel;
        }

        statement.close();

        return null;
    }

    public MoneyModel getUUIDByDatabase(Connection connection, String UUID, Double value) throws SQLException {
        MoneyDB moneyDB = new MoneyDB();
        MoneyModel moneyModel = moneyDB.get(connection, UUID);

        if (moneyModel == null) {
            moneyModel = new MoneyModel(UUID, value);
            moneyDB.create(connection, moneyModel);
        }

        return moneyModel;
    }
}
