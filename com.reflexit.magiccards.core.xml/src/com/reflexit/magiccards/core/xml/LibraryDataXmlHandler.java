package com.reflexit.magiccards.core.xml;

import org.eclipse.core.runtime.CoreException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.ICardStore;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;

public class LibraryDataXmlHandler extends AbstractFilteredCardStore<IMagicCard> {
	private static LibraryDataXmlHandler instance;
	private SingleFileCardStore table;

	@Override
	public int getSize() {
		return super.getSize();
	}

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	public void update(MagicCardFilter filter) throws MagicException {
		initialize();
		this.getFilteredList().clear();
		this.getFilteredList().addAll(this.table.filterCards(filter));
	}

	@Override
	protected Collection<IMagicCard> doCreateList() {
		return new ArrayList<IMagicCard>();
	}

	@Override
	protected void doInitialize() throws MagicException {
		this.table.initialize();
	}

	public static IFilteredCardStore<IMagicCard> getInstance() {
		if (instance == null)
			new LibraryDataXmlHandler();
		return instance;
	}

	private LibraryDataXmlHandler() {
		instance = this;
		File file = null;
		try {
			file = XmlCardHolder.getLibrary();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.table = new SingleFileCardStore(file);
	}
}
