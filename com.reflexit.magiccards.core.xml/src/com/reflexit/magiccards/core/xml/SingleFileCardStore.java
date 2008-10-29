package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class SingleFileCardStore extends MemoryCardStore {
	protected transient File file;

	public SingleFileCardStore(File file) {
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
}
