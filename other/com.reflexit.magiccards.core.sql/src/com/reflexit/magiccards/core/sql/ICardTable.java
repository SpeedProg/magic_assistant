package com.reflexit.magiccards.core.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.reflexit.magiccards.core.model.MagicCardFilter;

public interface ICardTable<T> {
	int getCardsCount(MagicCardFilter filter) throws SQLException;

	ResultSet getAllCards(MagicCardFilter filter, boolean lazy) throws SQLException;

	T createElementCurrent(ResultSet result, T object) throws SQLException;

	T createDefaultElement();

	int getTotalCount() throws SQLException;
}
