package com.reflexit.magiccards.core.xml;

import org.eclipse.core.runtime.CoreException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.ICardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.nav.Deck;

public class DeckXmlHandler extends AbstractFilteredCardStore<IMagicCard> {
	private DeckFileCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	public void update(MagicCardFilter filter) throws MagicException {
		initialize();
		this.getFilteredList().clear();
		this.getFilteredList().addAll(filterCards(filter));
	}

	public Collection<IMagicCard> filterCards(MagicCardFilter filter) throws MagicException {
		initialize();
		Comparator<IMagicCard> comp = MagicCardComparator.getComparator(filter.getSortIndex(), filter.isAscending());
		TreeSet<IMagicCard> filteredList = new TreeSet<IMagicCard>(comp);
		for (Iterator<IMagicCard> iterator = this.table.cardsIterator(); iterator.hasNext();) {
			IMagicCard elem = iterator.next();
			if (!filter.isFiltered(elem)) {
				filteredList.add(elem);
			}
		}
		return filteredList;
	}

	@Override
	protected Collection<IMagicCard> doCreateList() {
		return new ArrayList<IMagicCard>();
	}

	@Override
	protected void doInitialize() throws MagicException {
		this.table.initialize();
	}

	public DeckXmlHandler(String filename) {
		File file = null;
		try {
			Deck d = DataManager.getModelRoot().getDeck(filename);
			if (d == null)
				d = DataManager.getModelRoot().addDeck(filename);
			file = d.getFile();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.table = new DeckFileCardStore(file, null);
	}
}
