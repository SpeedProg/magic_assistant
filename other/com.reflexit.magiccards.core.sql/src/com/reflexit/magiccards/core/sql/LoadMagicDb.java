package com.reflexit.magiccards.core.sql;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.TextPrinter;

public class LoadMagicDb {
	public static void main(String[] args) throws IOException, SQLException {
		load(new File(args[0]));
	}

	static void load(File file) throws IOException, SQLException {
		BufferedReader st = new BufferedReader(new FileReader(file));
		load(st);
		st.close();
	}

	public static void load(BufferedReader st) throws IOException, SQLException {
		MagicDb.removeMagicCardsTable();
		MagicDb.createTable(MagicCardTable.getInstance());
		HashMap map = new HashMap();
		updateTable(st, map);
	}

	public static int updateTable(BufferedReader st) throws IOException, SQLException {
		Connection conn = MagicDb.createConnection();
		HashMap map = new HashMap();
		Statement stmt = conn.createStatement();
		ResultSet executeQuery = stmt.executeQuery("SELECT id FROM " + MagicCardTable.TABLE);
		while (executeQuery.next() != false) {
			int id = executeQuery.getInt(1);
			Integer key = new Integer(id);
			map.put(key, key);
		}
		stmt.close();
		int inserted = updateTable(st, map);
		return inserted;
	}

	public static void loadInitial() throws IOException, SQLException {
		InputStream is = FileLocator.openStream(Activator.getDefault().getBundle(), new Path("resources/all.txt"),
		        false);
		try {
			LoadMagicDb.load(new BufferedReader(new InputStreamReader(is)));
		} finally {
			is.close();
		}
	}

	public static int updateTable(BufferedReader st, HashMap map) throws IOException, SQLException {
		MagicDb.createConnection();
		Connection conn = MagicDb.getConnection();
		String line;
		st.readLine(); // header ignore for now
		int rec = 0;
		conn.setAutoCommit(false);
		try {
			while ((line = st.readLine()) != null) {
				String[] fields = line.split("\\Q" + TextPrinter.SEPARATOR);
				for (int i = 0; i < fields.length; i++) {
					fields[i] = fields[i].trim();
				}
				MagicCard card = new MagicCard();
				card.setValues(fields);
				card.setExtraFields();
				int id = card.getCardId();
				if (id == 0) {
					System.err.print("Skipped invalid: ");
					TextPrinter.print(card, System.err);
					continue;
				}
				Integer key = new Integer(id);
				if (map.containsKey(key)) {
					System.err.print("Skipped duplicate: ");
					TextPrinter.print(card, System.err);
					continue;
				}
				try {
					String insertStmt = MagicCardTable.INSTANCE.getInsertPrepare();
					PreparedStatement sta = conn.prepareStatement(insertStmt);
					MagicCardTable.getInstance().prepareInsertStmt(card, sta);
					sta.executeUpdate();
					sta.close();
					map.put(key, card);
					rec++;
				} catch (SQLException e) {
					System.err.print("Skipped error: " + e.getMessage());
					TextPrinter.print(card, System.err);
				}
			}
			conn.commit();
		} finally {
			conn.setAutoCommit(true);
		}
		return rec;
	}
}
