package moe.nmkmn.sharing_player_data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import jp.jyn.jecon.Jecon;
import moe.nmkmn.sharing_player_data.commands.SPDCommand;
import moe.nmkmn.sharing_player_data.listeners.PlayerJoinListener;
import moe.nmkmn.sharing_player_data.listeners.PlayerLeaveListener;
import moe.nmkmn.sharing_player_data.models.MoneyModel;
import moe.nmkmn.sharing_player_data.utils.Database;
import moe.nmkmn.sharing_player_data.utils.MoneyDB;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;

public final class Main extends JavaPlugin {
    private Jecon jecon;
    private DataSource dataSource;
    private static int increment = 1;
    private static boolean useMariaDbDriver = false;

    private static synchronized void increment() {
        increment++;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Jecon");

        if (plugin == null || !plugin.isEnabled()) {
            getLogger().warning("Jecon is not available.");
        }

        jecon = (Jecon) plugin;

        try {
            loadDataSource();
        } catch (RuntimeException e) {
            dataSource = null;
            useMariaDbDriver = true;
            loadDataSource();
        }

        // Database Initialize
        Database database;
        try {
            database = new Database(dataSource.getConnection());
            database.initialize();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Command Listeners
        Objects.requireNonNull(getCommand("spd")).setExecutor(new SPDCommand(this));

        // Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);

        // Setup
        Connection connection;
        try {
            connection = getDataSource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

//        if (connection != null) {
//            MoneyDB moneyDB = new MoneyDB();
//
//            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
//                OptionalDouble value = getJecon().getRepository().getDouble(player.getUniqueId());
//
//                if (value.isPresent()) {
//                    try {
//                        moneyDB.getUUIDByDatabase(connection, player.getUniqueId().toString(), value.getAsDouble());
//                    } catch (SQLException e) {
//                        getLogger().severe(e.getMessage());
//                    }
//                }
//            }
//        }

        // Schedule
        new BukkitRunnable() {
            @Override
            public void run() {
                if (connection != null) {
                    MoneyDB moneyDB = new MoneyDB();

                    for (OfflinePlayer player : Bukkit.getOnlinePlayers()) {
                        OptionalDouble value = getJecon().getRepository().getDouble(player.getUniqueId());

                        try {
                            MoneyModel moneyModel = moneyDB.get(connection, player.getUniqueId().toString());

                            if (value.isPresent()) {
                                if (moneyModel == null) {
                                    moneyDB.create(connection, new MoneyModel(player.getUniqueId().toString(), value.getAsDouble()));
                                } else  {
                                    moneyDB.update(connection, new MoneyModel(player.getUniqueId().toString(), value.getAsDouble()));
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 5 * 60 * 20L);
    }

    public void loadDataSource() {
        try {

            HikariConfig hikariConfig = new HikariConfig();

            String host = Objects.requireNonNull(getConfig().getString("database.host"));
            String port = Objects.requireNonNull(getConfig().getString("database.port"));
            String database = Objects.requireNonNull(getConfig().getString("database.database"));
            String launchOptions = Objects.requireNonNull(getConfig().getString("database.launch_options"));

            if (launchOptions.isEmpty() || !launchOptions.matches("\\?((([\\w-])+=.+)&)*(([\\w-])+=.+)")) {
                launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
            }

            hikariConfig.setDriverClassName(useMariaDbDriver ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver");
            String protocol = useMariaDbDriver ? "jdbc:mariadb" : "jdbc:mysql";
            hikariConfig.setJdbcUrl(protocol + "://" + host + ":" + port + "/" + database + launchOptions);

            String username = Objects.requireNonNull(getConfig().getString("database.user"));
            String password = Objects.requireNonNull(getConfig().getString("database.password"));

            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.addDataSourceProperty("connectionInitSql", "set time_zone = '+00:00'");

            hikariConfig.setPoolName("SPD Connection Pool-" + increment);
            increment();

            hikariConfig.setAutoCommit(false);
            hikariConfig.setMaximumPoolSize(getConfig().getInt("database.max_connections"));
            hikariConfig.setMaxLifetime(getConfig().getInt("database.max_lifetime") * 60000L);
            hikariConfig.setLeakDetectionThreshold((getConfig().getInt("database.max_lifetime") * 60000L) + TimeUnit.SECONDS.toMillis(4L));

            this.dataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            if (e.getMessage().contains("Unknown system variable 'transaction_isolation'")) {
                throw new RuntimeException("MySQL driver is incompatible with database that is being used.", e);
            }
            throw new RuntimeException("Failed to set-up HikariCP Datasource: " + e.getMessage(), e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Jecon getJecon() {
        return jecon;
    }
}
