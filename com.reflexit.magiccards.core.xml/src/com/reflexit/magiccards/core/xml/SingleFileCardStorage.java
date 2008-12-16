package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.AbstractStorage;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class SingleFileCardStorage extends AbstractStorage<IMagicCard> implements ILocatable {
	protected transient File file;
	protected MemoryCardStore store;
	protected String location;

	public SingleFileCardStorage(File file, String location) {
		this(file, location, false);
	}

	public SingleFileCardStorage(File file, String location, boolean initialized) {
		this.file = file;
		this.store = new MemoryCardStore<IMagicCard>();
		this.store.initialize();
		this.location = location;
		this.initialized = initialized;
	}

	@Override
	protected synchronized void doInitialize() {
		CardCollectionStoreObject obj = null;
		try {
			obj = CardCollectionStoreObject.initFromFile(this.file);
		} catch (IOException e) {
			Activator.log(e);
		}
		loadFields(obj);
		updateLocations();
	}

	void updateLocations() {
		if (getLocation() == null)
			return;
		for (Iterator iterator = cardsIterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof MagicCardPhisical) {
				MagicCardPhisical mp = (MagicCardPhisical) object;
				mp.setLocation(getLocation());
			}
		}
	}

	/**
	 * @param obj
	 */
	protected void loadFields(CardCollectionStoreObject obj) {
		if (obj.list != null)
			this.store.setList(obj.list);
		else
			this.store.setList(new ArrayList<IMagicCard>());
		if (obj.key != null)
			setLocation(obj.key);
	}

	/**
	 * @param obj
	 */
	protected void storeFields(CardCollectionStoreObject obj) {
		obj.list = (ArrayList) this.store.getList();
		obj.key = getLocation();
	}

	@Override
	public void save() {
		try {
			doSave();
		} catch (FileNotFoundException e) {
			throw new MagicException(e);
		}
	}

	@Override
	protected synchronized void doSave() throws FileNotFoundException {
		CardCollectionStoreObject obj = new CardCollectionStoreObject();
		obj.file = this.file;
		storeFields(obj);
		obj.save();
	}

	@Override
	protected boolean doAddCard(IMagicCard card) {
		return this.store.doAddCard(card);
	}

	@Override
	protected boolean doRemoveCard(IMagicCard card) {
		return this.store.doRemoveCard(card);
	}

	public Iterator<IMagicCard> cardsIterator() {
		return this.store.cardsIterator();
	}

	public int getTotal() {
		return this.store.getTotal();
	}

	@Override
	public void clear() {
		this.store = new MemoryCardStore<IMagicCard>();
		this.store.initialize();
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}
}
