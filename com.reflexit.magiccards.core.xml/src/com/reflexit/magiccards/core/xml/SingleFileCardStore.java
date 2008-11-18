package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractStorage;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class SingleFileCardStore extends AbstractStorage<IMagicCard> {
	protected transient File file;
	protected MemoryCardStore store;

	public SingleFileCardStore(File file) {
		this.file = file;
		this.store = new MemoryCardStore<IMagicCard>();
		this.store.initialize();
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
	}

	/**
	 * @param obj
	 */
	protected void loadFields(CardCollectionStoreObject obj) {
		if (obj.list != null)
			this.store.setList(obj.list);
		else
			this.store.setList(new ArrayList<IMagicCard>());
	}

	/**
	 * @param obj
	 */
	protected void storeFields(CardCollectionStoreObject obj) {
		obj.list = (ArrayList) this.store.getList();
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
}
