package com.reflexit.magiccards.core.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.derby.client.am.Types;

public abstract class XTable {
	static class NameType {
		String name;
		String type;
		String flags;

		public NameType(String name, String type) {
			this(name, type, "");
		}

		public NameType(String name, String type, String flags) {
			super();
			this.name = name;
			this.type = type;
			this.flags = flags;
		}
	}

	protected abstract NameType[] getTableHeader();

	public String getCreateTable() {
		StringBuffer buf = new StringBuffer("CREATE TABLE ");
		buf.append(getTableName());
		buf.append(" (");
		NameType[] nameTypes = getTableHeader();
		for (int i = 0; i < nameTypes.length; i++) {
			NameType el = nameTypes[i];
			buf.append(el.name);
			buf.append(" ");
			buf.append(el.type);
			if (i < nameTypes.length - 1) {
				buf.append(", ");
			}
		}
		buf.append(")");
		return buf.toString();
	}

	public String[] getIndexQueries() {
		return new String[0];
	}

	public static String join(Collection list) {
		return join(list, ", ");
	}

	public static String join(Collection list, String sep) {
		StringBuffer buf = new StringBuffer();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element != null)
				buf.append(element.toString());
			else
				buf.append("null");
			if (iter.hasNext()) {
				buf.append(sep);
			}
		}
		return buf.toString();
	}

	public Collection getNames() {
		NameType[] nameTypes = getTableHeader();
		ArrayList list = new ArrayList(nameTypes.length);
		for (int i = 0; i < nameTypes.length; i++) {
			NameType el = nameTypes[i];
			list.add(el.name);
		}
		return list;
	}

	public abstract String getTableName();

	public String getInsertPrepare() {
		Collection names = getNames();
		Collection ask = new ArrayList(names.size());
		for (Iterator iter = names.iterator(); iter.hasNext();) {
			iter.next();
			ask.add("?");
		}
		return "INSERT INTO " + getTableName() + " (" + join(names) + ") VALUES (" + join(ask) + ")";
	}

	protected void prepareInsertStmt(PreparedStatement st, Collection val) throws SQLException {
		int i = 0;
		for (Iterator iter = val.iterator(); iter.hasNext(); i++) {
			Object element = iter.next();
			int column = i + 1;
			if (element == null) {
				st.setString(column, null);
			} else if (element instanceof Integer) {
				st.setInt(column, ((Integer) element).intValue());
			} else if (element instanceof Float) {
				Float x = (Float) element;
				if (Float.compare(x.floatValue(), Float.NaN) == 0)
					st.setNull(column, Types.DECIMAL);
				else
					st.setFloat(column, x.floatValue());
			} else {
				st.setString(column, element.toString());
			}
		}
	}

	public int getTotalCount() throws SQLException {
		try {
			MagicDb.stmt = MagicDb.createStatement();
			String query = "SELECT count(*) FROM " + getTableName();
			ResultSet results = MagicDb.stmt.executeQuery(query);
			results.next();
			return results.getInt(1);
		} catch (SQLException e) {
			throw e;
		} finally {
			MagicDb.stmt.close();
			MagicDb.stmt = null;
		}
	}
}
