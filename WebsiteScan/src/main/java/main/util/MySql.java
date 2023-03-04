package main.util;

import main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class MySql {
	private static final String USER = "websitescan";
	private static final String PASSWORD = "websitescan";
	private static final Logger dblog = LoggerFactory.getLogger("DBLOG");

	private static Connection connection = null;

	public static boolean connect() {

		if (connection == null) {
			try {
				dblog.info("Connecting to database");
				connection = DriverManager.getConnection(
						"jdbc:mysql://localhost/websitescan?user=" + USER + "&password=" + PASSWORD);
			} catch (SQLException e) {
				dblog.error("Can't reach MySQL SERVER - SHUTDOWN");
				Main.INSTANCE.onShutdown();
				return false;
			}
		}

		return true;
	}

	public static ResultSet onQuery(String sqlPattern, Object... parameters) throws SQLException {
		dblog.debug("MySQL-Query requested - query: " + sqlPattern);

		if (!connect()) {
			return null;
		}

		if (parameters.length != countMatches(sqlPattern, "?")) {
			IllegalArgumentException e = new IllegalArgumentException(
					"Invalid SQLString! - parameter count does not match.", new Throwable().fillInStackTrace());
			dblog.error(e.getMessage(), e);
		}

		try {

			PreparedStatement p = connection.prepareStatement(sqlPattern);

			for (int i = 0; i < parameters.length; i++) {
				p.setObject(i + 1, parameters[i]);
			}

			return p.executeQuery();
		} catch (SQLException e) {
			dblog.error(e.getMessage(), e);
			return null;
		}
	}

	public static void onUpdate(String sqlPattern, Object... parameters) throws SQLException {
		dblog.debug("MySQL-Update requested - query: " + sqlPattern);

		if (!connect()) {
			return;
		}

		if (parameters.length != countMatches(sqlPattern, "?")) {
			IllegalArgumentException e = new IllegalArgumentException(
					"Invalid SQLString! - parameter count does not match.", new Throwable().fillInStackTrace());
			dblog.error(e.getMessage(), e);
		}

		try {
			PreparedStatement p = connection.prepareStatement(sqlPattern);

			for (int i = 0; i < parameters.length; i++) {
				p.setObject(i + 1, parameters[i]);
			}

			p.executeUpdate();

		} catch (SQLException e) {
			dblog.error(e.getMessage(), e);
		}
	}

	protected static int countMatches(String base, String pattern) {

		int occurences = 0;
		if (0 != pattern.length()) {
			for (int index = base.indexOf(pattern); index != -1; index = base.indexOf(pattern, index + 1)) {
				occurences++;
			}
		}
		return occurences;

	}
}
