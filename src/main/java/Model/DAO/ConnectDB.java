package Model.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectDB {

    private static HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/ltm?useSSL=false&useUnicode=true&characterEncoding=utf8");
        config.setUsername("root");
        config.setPassword("");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(150);
        config.setMinimumIdle(20); // Giữ ít nhất 20 connections sẵn sàng
        config.setConnectionTimeout(30000); // 30 giây timeout
        config.setIdleTimeout(600000); // 10 phút idle timeout
        config.setMaxLifetime(1800000); // 30 phút max lifetime
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    public static Connection connectDatabase() throws SQLException {
        return ds.getConnection();
    }
}
