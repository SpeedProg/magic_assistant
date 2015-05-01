package com.reflexit.magiccards.core.model.xml;

import java.util.Collection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
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
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

public class LibraryFilteredCardFileStore extends BasicLibraryFilteredCardFileStore implements
		ICardEventListener {
	private static LibraryFilteredCardFileStore instance;

	@Override
	protected void doInitialize() throws MagicException {
		MagicLogger.traceStart("lfcs init");
		ModelRoot container = getModelRoot();
		Collection<CardCollection> colls = container.getAllElements();
		// init super
		CardCollection def = getModelRoot().getDefaultLib();
		for (CardElement elem : colls) {
			table.addFile(elem.getFile(), elem.getLocation(), false);
		}
		table.setLocation(def.getLocation());
		table.initialize();
		table.addListener(this);
		initialized = true;
		container.addListener(this);
		reconcile();
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
	public void handleEvent(final CardEvent event) {
		switch (event.getType()) {
			case CardEvent.ADD:
			case CardEvent.REMOVE:
				return;
			case CardEvent.ADD_CONTAINER: {
				CardElement elem = (CardElement) event.getData();
				if (elem instanceof CardCollection) {
					CardCollection cardCollection = (CardCollection) elem;
					CollectionCardStore store = table
							.addFile(elem.getFile(), elem.getLocation());
					IStorage storage = store.getStorage();
					if (storage instanceof IStorageInfo) {
						((IStorageInfo) storage)
								.setType(cardCollection.isDeck() ? IStorageInfo.DECK_TYPE
										: IStorageInfo.COLLECTION_TYPE);
					}
					cardCollection.open(store);
				}
				break;
			}
			default:
				break;
		}
		new Thread("Lib event processing") {
			@Override
			public void run() {
				switch (event.getType()) {
					case CardEvent.ADD_CONTAINER: {
						CardElement elem = (CardElement) event.getData();
						if (elem instanceof CardCollection) {
							ICardStore<IMagicCard> store = getStore(elem.getLocation());
							DataManager.getInstance().reconcile(store);
							update();
						}
						break;
					}
					case CardEvent.REMOVE_CONTAINER: {
						CardElement elem = (CardElement) event.getData();
						ICardStore<IMagicCard> store = getStore(elem.getLocation());
						table.removeLocation(elem.getLocation());
						DataManager.getInstance().reconcile(store);
						update();
						break;
					}
					case CardEvent.RENAME_CONTAINER: {
						CardElement elem = (CardElement) event.getSource();
						// System.err.println("renamed " + event.getData() + " to " +
						// elem.getLocation());
						if (elem instanceof CardOrganizer) {
							// ignore, individual rename per element would be sent for
							// path and name changes
						} else {
							table.renameLocation((Location) event.getData(), elem.getLocation());
							ICardStore<IMagicCard> store = getStore(elem.getLocation());
							DataManager.getInstance().reconcile(store);
							update();
						}
						break;
					}
					case CardEvent.UPDATE_CONTAINER: {
						CardElement elem = (CardElement) event.getSource();
						if (elem instanceof CardCollection) {
							ICardStore<IMagicCard> store = getStore(elem.getLocation());
							DataManager.getInstance().reconcile(store);
							update();
						}
						break;
					}
					case CardEvent.UPDATE: {
						// need to save xml
						if (event.getData() instanceof MagicCardPhysical) {
							MagicCardPhysical c = (MagicCardPhysical) event.getData();
							Location location = c.getLocation();
							AbstractCardStoreWithStorage storage = table.getStorage(location);
							if (storage != null) {
								storage.getStorage().autoSave();
							}
						}
						break;
					}
					default:
						break;
				}
				return;
			}
		}.start();
	}

	private void reconcile() {
		DataManager.getInstance().reconcile();
	}
}
