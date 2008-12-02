package com.reflexit.magiccards.core.sql.handlers;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.AbstractCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.sql.MagicCardTable;

public class MagicCardDataHandler extends ResultSetDataHandler<IMagicCard> {
	private static MagicCardDataHandler instance = new MagicCardDataHandler();

	public static MagicCardDataHandler getInstance() {
		return instance;
	}

	private MagicCardDataHandler() {
		this.table = MagicCardTable.getInstance();
		this.store = new AbstractCardStore<IMagicCard>() {
			@Override
			protected void doAddAll(Collection cards) {
				// TODO Auto-generated method stub
			}

			@Override
			protected boolean doAddCard(IMagicCard card) {
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

			public int getTotal() {
				try {
					return MagicCardDataHandler.this.table.getTotalCount();
				} catch (SQLException e) {
					Activator.log(e);
					return 0;
				}
			}

			public void save() {
				// TODO Auto-generated method stub
			}
		};
	}
}
