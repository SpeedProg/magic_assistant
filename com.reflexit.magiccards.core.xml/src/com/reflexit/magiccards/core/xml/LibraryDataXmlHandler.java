package com.reflexit.magiccards.core.xml;

import java.util.Collection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.ICardStore;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;

public class LibraryDataXmlHandler extends AbstractFilteredCardStore<IMagicCard> {
	private static LibraryDataXmlHandler instance;
	private CollectionMultiFileCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	@Override
	protected void doInitialize() throws MagicException {
		CollectionsContainer container = DataManager.getModelRoot().getCollectionsContainer();
		Collection<CardElement> colls = container.getAllElements();
		// init super
		for (CardElement elem : colls) {
			this.table.addFile(elem.getResource().getLocation().toFile(), elem.getLocation());
		}
		this.table.initialize();
	}

	public static IFilteredCardStore getInstance() {
		if (instance == null)
			new LibraryDataXmlHandler();
		return instance;
	}

	private LibraryDataXmlHandler() {
		instance = this;
		this.table = new CollectionMultiFileCardStore();
	}
}
