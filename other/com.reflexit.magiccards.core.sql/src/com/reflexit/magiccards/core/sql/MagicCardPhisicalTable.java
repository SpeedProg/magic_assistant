package com.reflexit.magiccards.core.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

public class MagicCardPhisicalTable extends XTable implements ICardTable<MagicCardPhisical> {
	public static final String TABLE = "realcards";
	private static final MagicCardPhisicalTable INSTANCE = new MagicCardPhisicalTable();

	public static MagicCardPhisicalTable getInstance() {
		return INSTANCE;
	}
	static NameType dbTypes[] = { //
	new NameType("cid", "INT"), // foreign key to MagicCardPhisicals 
	        new NameType("count", "INT"), //
	        new NameType("price", "DECIMAL(7,2)"), //
	        new NameType("comment", "VARCHAR(1200)"), //
	        new NameType("location", "VARCHAR(80)"), //
	        new NameType("condition", "VARCHAR(40)"), //
	};

	public MagicCardPhisical createElementCurrent(ResultSet resultSet, MagicCardPhisical card) throws SQLException {
		String[] fields = new String[dbTypes.length];
		for (int i = 0; i < dbTypes.length; i++) {
			Object o = resultSet.getObject(i + 1);
			fields[i] = (o == null ? null : o.toString());
		}
		if (card == null)
			card = new MagicCardPhisical();
		card.setValues(fields);
		MagicCardTable mct = MagicCardTable.getInstance();
		fields = new String[mct.dbTypes.length];
		for (int i = 0; i < mct.dbTypes.length; i++) {
			Object o = resultSet.getObject(i + 1 + dbTypes.length);
			fields[i] = (o == null ? null : o.toString());
		}
		MagicCard card1 = new MagicCard();
		card1.setValues(fields);
		card.setMagicCard(card1);
		return card;
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
		long time = System.currentTimeMillis();
		try {
			MagicDb.stmt = MagicDb.createStatement();
			String condition = ConditionUtils.getCondition(filter);
			String query = "SELECT count(*) FROM " + getTableName() //
			        + " LEFT JOIN " + MagicCardTable.TABLE //
			        + " ON cid = id"//
			        + (condition == null ? "" : " WHERE " + condition);
			ResultSet results = MagicDb.stmt.executeQuery(query);
			results.next();
			return results.getInt(1);
		} catch (SQLException e) {
			throw e;
		} finally {
			System.err.println("count time: " + (System.currentTimeMillis() - time) + " ms");
			MagicDb.stmt.close();
			MagicDb.stmt = null;
		}
	}

	public ResultSet getAllCards(MagicCardFilter filter, boolean createCursor) throws SQLException {
		long time = System.currentTimeMillis();
		try {
			MagicDb.stmt = MagicDb.createStatement();
			String condition = ConditionUtils.getCondition(filter);
			String query = "SELECT * FROM " + getTableName() //
			        + " LEFT JOIN " + MagicCardTable.TABLE //
			        + " ON cid = id"//
			        + (condition == null ? "" : " WHERE " + condition);
			ResultSet results = MagicDb.stmt.executeQuery(query);
			return results;
		} catch (SQLException e) {
			MagicDb.stmt.close();
			MagicDb.stmt = null;
			throw e;
		} finally {
			System.err.println("getall time: " + (System.currentTimeMillis() - time) + " ms");
		}
	}

	public void prepareInsertStmt(MagicCardPhisical card, PreparedStatement st) throws SQLException {
		Collection val = card.getValues();
		prepareInsertStmt(st, val);
	}

	public MagicCardPhisical createDefaultElement() {
		return new MagicCardPhisical();
	}

	public boolean insertCard(MagicCard card) throws SQLException {
		String insertStmt = MagicCardPhisicalTable.getInstance().getInsertPrepare();
		PreparedStatement sta = MagicDb.getConnection().prepareStatement(insertStmt);
		MagicCardPhisical card2 = new MagicCardPhisical();
		card2.setMagicCard(card);
		MagicCardPhisicalTable.getInstance().prepareInsertStmt(card2, sta);
		System.err.println(insertStmt);
		sta.executeUpdate();
		sta.close();
		return true;
	}
}
