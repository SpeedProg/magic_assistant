package com.reflexit.magiccards.core.xml;

import java.util.Collection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

public class LibraryFilteredCardFileStore extends BasicLibraryFilteredCardFileStore implements ICardEventListener {
	private static LibraryFilteredCardFileStore instance;

	@Override
	protected synchronized void doInitialize() throws MagicException {
		MagicLogger.traceStart("lfcs init");
		ModelRoot container = DataManager.getModelRoot();
		Collection<CardElement> colls = container.getAllElements();
		// init super
		CardCollection def = DataManager.getModelRoot().getDefaultLib();
		for (CardElement elem : colls) {
			this.table.addFile(elem.getFile(), elem.getLocation(), false);
		}
		this.table.setLocation(def.getLocation());
		this.table.initialize();
		container.addListener(this);
		table.addListener(this);
		initialized = true;
		DataManager.reconcile();
		MagicLogger.traceEnd("lfcs init");
	}

	public synchronized static LibraryFilteredCardFileStore getInstance() {
		if (instance == null)
			instance = new LibraryFilteredCardFileStore();
		return instance;
	}

	private LibraryFilteredCardFileStore() {
		super(new CollectionMultiFileCardStore());
	}

	@Override
	public void handleEvent(CardEvent event) {
		if (event.getData() instanceof CardElement) {
			CardElement elem = (CardElement) event.getData();
			if (event.getType() == CardEvent.ADD_CONTAINER) {
				if (elem instanceof CardCollection) {
					CollectionCardStore store = this.table.addFile(elem.getFile(), elem.getLocation());
					IStorage storage = store.getStorage();
					if (storage instanceof IStorageInfo) {
						((IStorageInfo) storage).setType(((CardCollection) elem).isDeck() ? IStorageInfo.DECK_TYPE
								: IStorageInfo.COLLECTION_TYPE);
					}
					DataManager.reconcile();
					update();
				}
			} else if (event.getType() == CardEvent.REMOVE_CONTAINER) {
				this.table.removeLocation(elem.getLocation());
				DataManager.reconcile();
				update();
			}
		} else if (event.getSource() instanceof CardElement) {
			CardElement elem = (CardElement) event.getSource();
			if (event.getType() == CardEvent.RENAME_CONTAINER) {
				// System.err.println("renamed " + event.getData() + " to " +
				// elem.getLocation());
				if (elem instanceof CardOrganizer) {
					// ignore, individual rename per element would be sent for
					// path and name changes
				} else {
					this.table.renameLocation((Location) event.getData(), elem.getLocation());
					DataManager.reconcile();
					update();
				}
			}
		} else if (event.getType() == CardEvent.UPDATE) { // XXX: UPDATE_LIST
			// need to save xml
			if (event.getData() instanceof MagicCardPhysical) {
				MagicCardPhysical c = (MagicCardPhysical) event.getData();
				Location location = c.getLocation();
				AbstractCardStoreWithStorage storage = table.getStorage(location);
				if (storage != null) {
					storage.getStorage().save();
				}
			}
		}
	}
}
