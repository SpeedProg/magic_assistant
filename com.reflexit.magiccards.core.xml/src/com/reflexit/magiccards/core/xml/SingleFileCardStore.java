package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class SingleFileCardStore extends AbstractCardStore<IMagicCard> {
	private ArrayList<IMagicCard> list;
	protected transient File file;

	public SingleFileCardStore(File file) {
		this.list = null;
		this.file = file;
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
			this.list = obj.list;
		else
			this.list = new ArrayList<IMagicCard>();
	}

	/**
	 * @param obj
	 */
	protected void storeFields(CardCollectionStoreObject obj) {
		obj.list = this.getList();
	}

	public Iterator<IMagicCard> cardsIterator() {
		return this.getList().iterator();
	}

	@Override
	public void doRemoveCard(IMagicCard card) {
		this.getList().remove(card);
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		return this.getList().add(card);
	}

	public void save() {
		try {
			doSave();
		} catch (FileNotFoundException e) {
			throw new MagicException(e);
		}
	}

	protected synchronized void doSave() throws FileNotFoundException {
		CardCollectionStoreObject obj = new CardCollectionStoreObject();
		obj.file = this.file;
		storeFields(obj);
		obj.save();
	}

	@Override
	protected void doAddAll(Collection cards) {
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard object = (IMagicCard) iterator.next();
			doAddCard(object);
		}
	}

	public int getTotal() {
		return this.getList().size();
	}

	/**
	 * @return the list
	 */
	public ArrayList<IMagicCard> getList() {
		if (this.list == null)
			doInitialize();
		return this.list;
	}
}
