package repositories;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnector {

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        System.out.println(url + user + password);

        if (url == null || user == null || password == null) {

            try (InputStream input = DBConnector.class.getResourceAsStream("/db.properties")) {

                if (input == null) {
                    throw new RuntimeException("Unable to find db.properties");
                }

                Properties properties = new Properties();
                properties.load(input);

                config.setJdbcUrl(properties.getProperty("db.url"));
                config.setUsername(properties.getProperty("db.username"));
                config.setPassword(properties.getProperty("db.password"));

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        } else {

            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
        }

        ds = new HikariDataSource(config);

    }

    private DBConnector() {

    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void close() {

        if (ds != null) {
            ds.close();
        }

    }

}
