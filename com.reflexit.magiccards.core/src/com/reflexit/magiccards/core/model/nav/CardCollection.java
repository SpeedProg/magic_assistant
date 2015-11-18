package com.reflexit.magiccards.core.model.nav;

import java.io.File;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

/**
 * It is object representing a Deck or Collection (but not the card it contains)
 *
 * @author Alena
 *
 */
public class CardCollection extends CardElement {
	transient private ICardStore<IMagicCard> store;
	transient protected Boolean deck;
	transient protected Boolean virtual;
	transient protected boolean opening = false;

	public CardCollection(String filename, CardOrganizer parent) {
		this(filename, parent, null, null);
	}

	public CardCollection(String filename, CardOrganizer parent, Boolean deck, Boolean virtual) {
		super(filename, parent, false);
		this.deck = deck;
		this.virtual = virtual;
		setParentInit(parent);
		createFile();
		parent.fireCreationEvent(this);
	}

	private void createFile() {
		File file = getFile();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			throw new MagicException("Cannot create: " + file, e);
		}
	}

	@Override
	public CardElement newElement(String name, CardOrganizer parent) {
		return new CardCollection(name + ".xml", parent, null, null);
	}

	public ICardStore<IMagicCard> getStore() {
		if (store == null) {
			open();
		}
		return this.store;
	}

	public IStorageInfo getStorageInfo() {
		try {
			if (getStore() == null)
				return null;
		} catch (MagicException e) {
			//MagicLogger.log(e);
			return null;
		}
		IStorage storage = ((IStorageContainer) getStore()).getStorage();
		if (storage instanceof IStorageInfo) {
			IStorageInfo si = ((IStorageInfo) storage);
			return si;
		}
		return null;
	}

	@Override
	public String getName() {
		return super.getName();
	}

	public synchronized void open() {
		if (opening)
			return;
		opening = true;
		try {
			IFilteredCardStore<IMagicCard> fi = DataManager.getInstance().getCardHandler()
					.getCardCollectionFilteredStore(getId());
			associate(fi.getCardStore());
		} finally {
			opening = false;
		}
	}

	public synchronized void associate(ICardStore<IMagicCard> store) {
		if (store == null)
			return;
		this.store = store;
		IStorageInfo info = getStorageInfo();
		if (info != null) {
			if (deck != null) {
				info.setType(deck ? IStorageInfo.DECK_TYPE : IStorageInfo.COLLECTION_TYPE);
			}
			if (virtual != null) {
				info.setVirtual(virtual);
			}
		}
	}

	public boolean isDeck() {
		IStorageInfo info = getStorageInfo();
		if (info != null) {
			return IStorageInfo.DECK_TYPE.equals(info.getType());
		}
		if (deck != null)
			return deck;
		return false;
	}

	public void close() {
		this.store = null;
	}

	public String getId() {
		return getPath().getId();
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
		IStorageInfo info = getStorageInfo();
		if (info != null) {
			info.setVirtual(virtual);
		}
	}

	public boolean isVirtual() {
		IStorageInfo info = getStorageInfo();
		if (info != null) {
			return info.isVirtual();
		}
		if (virtual != null)
			return virtual;
		return true;
	}
}
