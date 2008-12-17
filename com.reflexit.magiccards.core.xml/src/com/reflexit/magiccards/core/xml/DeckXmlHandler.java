package com.reflexit.magiccards.core.xml;

import java.io.File;

import org.eclipse.core.runtime.CoreException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;

public class DeckXmlHandler extends AbstractFilteredCardStore<IMagicCard> implements ILocatable {
	private DeckFileCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
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
				d = DataManager.getModelRoot().getDeckContainer().addDeck(filename);
			file = d.getFile();
			if (!d.isOpen()) {
				d.open(new DeckFileCardStore(file, null, d.getLocation()));
			}
			this.table = (DeckFileCardStore) d.getStore();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getLocation() {
		return table.getLocation();
	}

	@Override
	public void setLocation(String location) {
		table.setLocation(location);
	}
}
