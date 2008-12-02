package com.reflexit.magiccards.core.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;

public class MagicCardTable extends XTable implements ICardTable<IMagicCard> {
	public static final String TABLE = "magiccards";
	public static final MagicCardTable INSTANCE = new MagicCardTable();

	public static MagicCardTable getInstance() {
		return INSTANCE;
	}
	static NameType dbTypes[] = { //
	new NameType("id", "INT", "primary key"), //
	        new NameType("name", "VARCHAR(147)"), // 147 - for Our Market Research Shows... Elemental
	        new NameType("cost", "VARCHAR(40)"), //
	        new NameType("type", "VARCHAR(80)"), //
	        new NameType("power", "DECIMAL(4,1)"), //
	        new NameType("toughness", "DECIMAL(4,1)"), //
	        new NameType("oracleText", "VARCHAR(1200)"), //
	        new NameType("edition", "VARCHAR(80)"), //
	        new NameType("rarity", "VARCHAR(40)"), //
	        new NameType("colorType", "VARCHAR(10)"), //
	        new NameType("cmc", "INT"), //
	};

	public IMagicCard createElementCurrent(ResultSet resultSet, IMagicCard card) throws SQLException {
		String[] fields = new String[dbTypes.length];
		for (int i = 0; i < dbTypes.length; i++) {
			Object o = resultSet.getObject(i + 1);
			fields[i] = (o == null ? null : o.toString());
		}
		if (card == null)
			card = new MagicCard();
		if (card instanceof MagicCard) {
			((MagicCard) card).setValues(fields);
		}
		return card;
	}

	@Override
	public String[] getIndexQueries() {
		return new String[] {// 
		"CREATE INDEX iname ON " + getTableName() + " (name)", //
		        "CREATE INDEX iid ON " + getTableName() + " (id)" };
	}

	@Override
	protected NameType[] getTableHeader() {
		return dbTypes;
	}

	@Override
	public String getTableName() {
		return TABLE;
	}

	public int getCardsCount(MagicCardFilter filter) throws SQLException {
		try {
			MagicDb.createConnection();
			MagicDb.stmt = MagicDb.conn.createStatement();
			// stmt.setCursorName("magiccursor3");
			long time = System.currentTimeMillis();
			String condition = ConditionUtils.getCondition(filter);
			String query = "SELECT count(DISTINCT name) FROM " + getTableName() //
			        + (condition == null ? "" : " WHERE " + condition);
			ResultSet results = MagicDb.stmt.executeQuery(query);
			results.next();
			System.err.println("count time: " + (System.currentTimeMillis() - time) + " ms");
			return results.getInt(1);
		} catch (SQLException e) {
			MagicDb.stmt.close();
			throw e;
		}
	}

	@Override
	public int getTotalCount() throws SQLException {
		try {
			MagicDb.createConnection();
			MagicDb.stmt = MagicDb.conn.createStatement();
			// stmt.setCursorName("magiccursor3");
			long time = System.currentTimeMillis();
			String query = "SELECT count(*) FROM " + getTableName();
			ResultSet results = MagicDb.stmt.executeQuery(query);
			results.next();
			System.err.println("count time: " + (System.currentTimeMillis() - time) + " ms");
			return results.getInt(1);
		} catch (SQLException e) {
			MagicDb.stmt.close();
			throw e;
		}
	}

	public ResultSet getAllCards(MagicCardFilter filter, boolean createCursor) throws SQLException {
		try {
			// select * from test where test.id in (select max(id) from test
			// group by name);
			long time = System.currentTimeMillis();
			MagicDb.createConnection();
			try {
				MagicDb.stmt = MagicDb.conn.createStatement();
				MagicDb.stmt.execute("DROP VIEW maxi");
			} catch (Exception e) {
				// ignore
				e.printStackTrace();
			}
			MagicDb.stmt = MagicDb.conn.createStatement();
			String condition = ConditionUtils.getCondition(filter);
			String subquery = "SELECT max(id) FROM " + getTableName() //
			        + (condition == null ? "" : " WHERE " + condition) // apply condition  here
			        + " GROUP BY name"; // distinct name
			String createView = "CREATE VIEW maxi (mid) AS " + subquery;
			MagicDb.stmt.execute(createView);
			MagicDb.stmt = createCursor //
			        ? MagicDb.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
			        : MagicDb.conn.createStatement();
			// stmt.setCursorName("magiccursor3");
			MagicDb.stmt.setFetchSize(300);
			String query = "SELECT " + getTableName() + ".* FROM maxi" //
			        + " LEFT JOIN " + getTableName() //
			        + " ON mid = id"//
			        + " ORDER BY " + (filter.getSortIndex() + 1) + " " + (filter.isAscending() ? "ASC" : "DESC");
			System.err.println("->" + query);
			ResultSet results = MagicDb.stmt.executeQuery(query);
			System.err.println("full time: " + (System.currentTimeMillis() - time) + " ms");
			return results;
		} catch (SQLException e) {
			MagicDb.stmt.close();
			throw e;
		}
	}

	public void prepareInsertStmt(MagicCard card, PreparedStatement st) throws SQLException {
		Collection val = card.getValues();
		prepareInsertStmt(st, val);
	}

	public IMagicCard createDefaultElement() {
		return new MagicCard();
	}
}
