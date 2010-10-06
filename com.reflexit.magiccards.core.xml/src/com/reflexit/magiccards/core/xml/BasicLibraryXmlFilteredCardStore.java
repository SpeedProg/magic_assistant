package com.reflexit.magiccards.core.xml;

import java.util.Collection;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class BasicLibraryXmlFilteredCardStore extends AbstractFilteredCardStore<IMagicCard> implements ICardCountable {
	protected CollectionMultiFileCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		initialize();
		return this.table;
	}

	public BasicLibraryXmlFilteredCardStore(CollectionMultiFileCardStore store) {
		table = store;
	}

	@Override
	protected void doInitialize() throws MagicException {
		if (table == null)
			throw new IllegalStateException();
	}

	@Override
	protected void reload() {
		this.table.setInitialized(false);
		super.reload();
		update();
	}

	public int getCount() {
		initialize();
		int count = 0;
		Collection<IMagicCard> list = getFilteredList();
		for (Object element : list) {
			IMagicCard magicCard = (IMagicCard) element;
			if (magicCard instanceof ICardCountable) {
				count += ((ICardCountable) magicCard).getCount();
			}
		}
		return count;
	}

	public ICardStore<IMagicCard> getStore(Location location) {
		initialize();
		if (location == null)
			return table.getStore(table.getLocation());
		return table.getStore(location);
	}
}
