package com.reflexit.magiccards.core.sql.handlers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.DirectAccessCollection;
import com.reflexit.magiccards.core.model.ICardStore;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.sql.ICardTable;

public abstract class ResultSetDataHandler<T> extends AbstractFilteredCardStore<T> {
	ResultSet result;
	ICardTable<T> table;
	protected int size;
	private boolean lazy;
	ICardStore store;

	public final boolean isLazy() {
		return this.lazy;
	}

	public final void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public ResultSet getResult() {
		return this.result;
	}

	public ICardStore getCardStore() {
		return this.store;
	}

	public void setResult(int size, ResultSet result) {
		this.result = result;
		this.size = size;
		resetFilteredList();
	}

	@Override
	public int getSize() {
		return this.size;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IFilteredCardStore#update(com.reflexit.magiccards.core.model.MagicCardFilter)
	 */
	public void update(MagicCardFilter filter) throws MagicException {
		try {
			if (this.result != null)
				this.result.close();
			this.size = this.table.getCardsCount(filter);
			this.result = this.table.getAllCards(filter, isLazy());
		} catch (SQLException e) {
			this.result = null;
			this.size = 0;
			throw new MagicException(e);
		}
		resetFilteredList();
	}

	protected T createElement(int index) {
		T row;
		try {
			this.result.absolute(index + 1);
			row = this.table.createElementCurrent(this.result, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			row = this.table.createDefaultElement();
		}
		try {
			setCard(index, row);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return row;
	}

	protected void setCard(int index, T row) {
		((DirectAccessCollection<T>) getFilteredList()).set(index, row);
	}

	@Override
	protected Collection<T> doCreateList() {
		return new DirectAccessCollection<T>();
	}

	@Override
	protected void doInitialize() {
		if (isLazy())
			return;
		try {
			while (this.result.next() == true) {
				T row;
				row = this.table.createElementCurrent(this.result, null);
				addFilteredCard(row);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IFilteredCardStore#getColumnCount()
	 */
	public int getColumnCount() {
		try {
			ResultSet result = getResult();
			if (result != null) {
				ResultSetMetaData metaData = result.getMetaData();
				int columnCount = metaData.getColumnCount();
				return columnCount;
			}
			return 0;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IFilteredCardStore#getColumnName(int)
	 */
	public String getColumnName(int i) {
		try {
			ResultSet result = getResult();
			if (result != null) {
				ResultSetMetaData metaData = result.getMetaData();
				return (metaData.getColumnLabel(i + 1));
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		return "?";
	}
}
