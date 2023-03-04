package main.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class SQLManager {

	private final static Logger log = LoggerFactory.getLogger(SQLManager.class);

	public static void initializeDB() {
		try {

			MySql.onUpdate("CREATE TABLE IF NOT EXISTS websites(website LONGTEXT, enabled BOOLEAN)");
			MySql.onUpdate(
					"CREATE TABLE IF NOT EXISTS pinglogs(website LONGTEXT, timestamp DATETIME, success BOOLEAN, ping INT , ping_errorcode LONGTEXT)");

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

	}

}
