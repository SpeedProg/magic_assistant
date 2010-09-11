package com.reflexit.magiccards.core.model.nav;

import java.io.File;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

public class CardCollection extends CardElement {
	transient private ICardStore<IMagicCard> store;
	transient protected boolean deck;
	transient boolean pendingVirtual;
	transient private boolean virtualFlag;

	public CardCollection(String filename, CardOrganizer parent) {
		this(filename, parent, false);
	}

	public CardCollection(String filename, CardOrganizer parent, boolean deck) {
		super(filename, parent, false);
		this.deck = deck;
		createFile();
		setParentInit(parent);
	}

	private void createFile() {
		try {
			File file = getFile();
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			throw new MagicException(e);
		}
	}

	@Override
	public CardElement newElement(String name, CardOrganizer parent) {
		return new CardCollection(name + ".xml", parent, isDeck());
	}

	public ICardStore<IMagicCard> getStore() {
		return this.store;
	}

	public IStorageInfo getStorageInfo() {
		if (store instanceof IStorageContainer) {
			IStorage storage = ((IStorageContainer) store).getStorage();
			if (storage instanceof IStorageInfo) {
				IStorageInfo si = ((IStorageInfo) storage);
				return si;
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return super.getName();
	}

	public void open(ICardStore<IMagicCard> store) {
		if (this.store == null) {
			this.store = store;
			IStorageInfo info = getStorageInfo();
			if (info != null) {
				deck = IStorageInfo.DECK_TYPE.equals(info.getType());
				if (pendingVirtual) {
					setVirtual(virtualFlag);
					pendingVirtual = false;
				}
			}
		} else {
			throw new IllegalArgumentException("Already open");
		}
	}

	public boolean isDeck() {
		return deck;
	}

	public void close() {
		this.store = null;
	}

	public boolean isOpen() {
		return this.store != null;
	}

	/**
	 * @return
	 */
	public String getFileName() {
		return getPath().lastSegment();
	}

	public void setVirtual(boolean virtual) {
		if (!isOpen()) {
			pendingVirtual = true;
			virtualFlag = virtual;
		} else if (store instanceof IStorageContainer) {
			IStorage storage = ((IStorageContainer) store).getStorage();
			if (storage instanceof IStorageInfo) {
				((IStorageInfo) storage).setVirtual(virtual);
				storage.save();
			}
		}
	}
}
