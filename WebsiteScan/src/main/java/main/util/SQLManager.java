package main.util;

import java.sql.SQLException;

public class SQLManager {

    public static void initializeDB() {
        try {

            MySql.onUpdate("CREATE TABLE IF NOT EXISTS websites(website LONGTEXT, enabled BOOLEAN)");
            MySql.onUpdate("CREATE TABLE IF NOT EXISTS pinglogs(website LONGTEXT, timestamp DATETIME, success BOOLEAN, ping_errorcode LONGTEXT)");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
