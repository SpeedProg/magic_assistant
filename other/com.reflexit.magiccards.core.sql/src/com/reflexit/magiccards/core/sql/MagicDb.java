package com.reflexit.magiccards.core.sql;

import org.eclipse.core.runtime.IPath;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.MagicCardFilter;

public class MagicDb {
	// private static String dbURL =
	// "jdbc:derby://localhost:1527/myDB;create=true;user=me;password=mine";
	private static String dbURL = "jdbc:derby:magicDB;create=true";
	// jdbc Connection
	public static Connection conn = null;
	public static Statement stmt = null;

	public static Connection getConnection() {
		return createConnection();
	}

	public static void main(String[] args) {
		createConnection();
		createMagicDbTables();
		printData();
		// shutdown();
		conn = null;
	}

	public static Connection createConnection() {
		try {
			if (conn == null) {
				Activator plugin = Activator.getDefault();
				if (plugin != null) {
					IPath stateLocation = plugin.getStateLocation();
					String storage = stateLocation.addTrailingSeparator().append("db").toOSString();
					System.setProperty("derby.system.home", storage);
				}
				// Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
				// Get a connection
				conn = DriverManager.getConnection(dbURL);
				createMagicDbTables();
			}
		} catch (Exception except) {
			except.printStackTrace();
		}
		return conn;
	}

	public static void createMagicDbTables() {
		try {
			createTable(MagicCardTable.getInstance());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			createTable(MagicCardPhisicalTable.getInstance());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createTable(XTable table) throws SQLException {
		String create = table.getCreateTable();
		execute(create);
		String[] indexQueries = table.getIndexQueries();
		for (int i = 0; i < indexQueries.length; i++) {
			String query = indexQueries[i];
			execute(query);
		}
	}

	public static void removeTable(XTable table) throws SQLException {
		execute("DROP TABLE " + table.getTableName());
	}

	public static void execute(String query) throws SQLException {
		Statement stmt = createStatement();
		System.err.println(query);
		stmt.execute(query);
		stmt.close();
	}

	private static void printData() {
		try {
			ResultSet results = MagicCardTable.getInstance().getAllCards(new MagicCardFilter(), true);
			ResultSetMetaData rsmd = results.getMetaData();
			int numberCols = rsmd.getColumnCount();
			for (int i = 1; i <= numberCols; i++) {
				// print Column Names
				System.out.print(rsmd.getColumnLabel(i) + "|");
			}
			System.out.println("\n-------------------------------------------------");
			while (results.next()) {
				for (int i = 1; i <= numberCols; i++) {
					// print Column Names
					System.out.print(results.getString(i) + "|");
				}
			}
			results.close();
			stmt.close();
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}

	public static void shutdown() {
		try {
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				DriverManager.getConnection(dbURL + ";shutdown=true");
				conn.close();
				conn = null;
			}
		} catch (SQLException sqlExcept) {
		}
	}

	public static void removeMagicCardsTable() throws SQLException {
		try {
			execute("DROP VIEW maxi");
		} catch (SQLException e) {
			// ignore
		}
		try {
			removeTable(MagicCardTable.getInstance());
		} catch (SQLException e) {
			// ignore
		}
	}

	public static Statement createStatement() throws SQLException {
		return createConnection().createStatement();
	}
}
