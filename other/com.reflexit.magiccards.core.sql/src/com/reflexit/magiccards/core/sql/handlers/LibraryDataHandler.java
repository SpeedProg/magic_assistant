package com.reflexit.magiccards.core.sql.handlers;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.sql.MagicCardPhisicalTable;

public class LibraryDataHandler extends ResultSetDataHandler<MagicCardPhisical> {
	private static LibraryDataHandler instance = new LibraryDataHandler();

	public static LibraryDataHandler getInstance() {
		return instance;
	}

	public LibraryDataHandler() {
		this.table = MagicCardPhisicalTable.getInstance();
		this.store = new AbstractCardStore<IMagicCard>() {
			public int getTotal() {
				try {
					return LibraryDataHandler.this.table.getTotalCount();
				} catch (SQLException e) {
					Activator.log(e);
					return 0;
				}
			}

			@Override
			protected void doAddAll(Collection cards) {
				// TODO Auto-generated method stub
			}

			@Override
			protected boolean doAddCard(IMagicCard card) {
				if (card instanceof MagicCard) {
					try {
						return ((MagicCardPhisicalTable) LibraryDataHandler.this.table).insertCard((MagicCard) card);
					} catch (SQLException e) {
						throw new MagicException(e);
					}
				}
				throw new UnsupportedOperationException();
			}

			@Override
			protected void doRemoveCard(IMagicCard card) {
				// TODO Auto-generated method stub
			}

			public Iterator cardsIterator() {
				// TODO Auto-generated method stub
				return null;
			}

			public void save() {
				// TODO Auto-generated method stub
			}
		};
	}
}
