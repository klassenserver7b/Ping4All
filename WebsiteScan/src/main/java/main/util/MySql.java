package main.util;

import main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySql {
    private static final String USER = "websitescan";
    private static final String PASSWORD = "websitescan";
    private static Logger dblog = LoggerFactory.getLogger("DBLOG");

    private static Connection connection = null;

    public static boolean connect() {

        if (connection == null) {
            try {
                dblog.info("Connecting to database");
                connection = DriverManager.getConnection("jdbc:mysql://localhost/websitescan?user=" + USER + "&password=" + PASSWORD);
            } catch (SQLException e) {
                dblog.error("Can't reach MySQL SERVER - SHUTDOWN");
                Main.INSTANCE.onShutdown();
                return false;
            }
        }

        return true;
    }

    public static ResultSet onQuery(String sql) throws SQLException {
            dblog.debug("MySQL-Query requested - query: " + sql);
            if (connect()) return connection.createStatement().executeQuery(sql);
            return null;
    }

    public static void onUpdate(String sql) throws SQLException {
            dblog.debug("MySQL-Update requested - query: " + sql);
            if (connect()) connection.createStatement().execute(sql);
    }

}
