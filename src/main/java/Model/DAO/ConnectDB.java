package Model.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectDB {
    
    private static HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/pdftodoc_db");
        config.setUsername("root");
        config.setPassword(""); // ⚠️ ĐIỀN MẬT KHẨU MYSQL CỦA BẠN VÀO ĐÂY
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // Cấu hình tối ưu HikariCP
        config.setMaximumPoolSize(10); 
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    public static Connection connectDatabase() throws SQLException {
        return ds.getConnection();
    }
}

