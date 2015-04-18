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

	public CardCollection(String filename, CardOrganizer parent) {
		this(filename, parent, null);
	}

	public CardCollection(String filename, CardOrganizer parent, Boolean deck) {
		super(filename, parent, false);
		this.deck = deck;
		setParentInit(parent);
		createFile();
		parent.fireCreationEvent(this);
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
		if (!isOpen()) {
			open();
		}
		IStorage storage = ((IStorageContainer) store).getStorage();
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

	public void open() {
		IFilteredCardStore<IMagicCard> fi = DataManager.getInstance().getCardHandler()
				.getCardCollectionFilteredStore(getPath().toString());
		open(fi.getCardStore());
	}

	public void open(ICardStore<IMagicCard> store) {
		if (store == null)
			return;
		this.store = store;
		IStorageInfo info = getStorageInfo();
		if (info != null) {
			if (deck != null) {
				if (deck)
					info.setType(IStorageInfo.DECK_TYPE);
				else
					info.setType(IStorageInfo.COLLECTION_TYPE);
			} else {
				deck = IStorageInfo.DECK_TYPE.equals(info.getType());
			}
			if (virtual != null) {
				info.setVirtual(virtual);
			} else {
				virtual = info.isVirtual();
			}
		}
	}

	public boolean isDeck() {
		if (deck == null) return false;
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
		return getPath().toString();
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
		if (isOpen()) {
			IStorageInfo info = getStorageInfo();
			if (info != null) {
				info.setVirtual(virtual);
			}
		}
	}

	public boolean isVirtual() {
		if (virtual == null && !isOpen())
			throw new IllegalArgumentException("Store is not open");
		if (virtual != null)
			return virtual;
		return true;
	}
}
