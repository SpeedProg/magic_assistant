package com.reflexit.magiccards.core.model.xml;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.CardList;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.CardEventUpdate;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class LibraryCardStore extends CollectionMultiFileCardStore {
	private static LibraryCardStore instance;

	public synchronized static LibraryCardStore getInstance() {
		if (instance == null)
			instance = new LibraryCardStore();
		return instance;
	}

	@Override
	public synchronized void doInitialize() {
		final ModelRoot container = DataManager.getInstance().getModelRoot();
		CollectionMultiFileCardStore table = this;
		Collection<CardCollection> colls = container.getAllElements();
		// init super
		CardCollection def = container.getDefaultLib();
		for (CardElement elem : colls) {
			table.addFile(elem.getFile(), elem.getLocation(), false);
		}
		table.setLocation(def.getLocation());
		super.doInitialize();
		initialized = true;
		EventListener lis = new EventListener();
		DataManager.getInstance().getModelRoot().addListener(lis);
	}

	@Override
	public void handleEvent(CardEvent event) {
		switch (event.getType()) {
		case CardEvent.UPDATE:
			// need to save xml. Add/Remove save automatically since there is add/remove all,
			// but there nothing triggers save on update except this
			CardEventUpdate evupdate = (CardEventUpdate) event;
			CardList<? extends ICard> cardList = evupdate.getCardList();
			Set<Location> locations = cardList.getUnique(MagicCardField.LOCATION);
			for (Location location : locations) {
				AbstractCardStoreWithStorage storage = getMultiStore().getStorage(location);
				if (storage != null) {
					storage.getStorage().autoSave();
				}
			}
			break;
		}
		super.handleEvent(event);
	}

	class EventListener implements ICardEventListener {
		@Override
		public void handleEvent(final CardEvent event) {
			switch (event.getType()) {
			case CardEvent.ADD:
			case CardEvent.REMOVE:
			case CardEvent.UPDATE:
				return;
			case CardEvent.ADD_CONTAINER: {
				CardElement elem = (CardElement) event.getData();
				if (elem instanceof CardCollection) {
					synchronized (elem) {
						CardCollection cardCollection = (CardCollection) elem;
						CollectionCardStore store = getMultiStore().addFile(elem.getFile(), elem.getLocation());
						cardCollection.associate(store);
					}
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
						}
						break;
					}
					case CardEvent.REMOVE_CONTAINER: {
						CardElement elem = (CardElement) event.getData();
						if (elem instanceof CardCollection) {
							ICardStore<IMagicCard> store = getStore(elem.getLocation());
							if (store != null) {
								DataManager.getInstance().remove(null, store);
								getMultiStore().removeLocation(elem.getLocation());
								DataManager.getInstance().reconcile(store);
							}
						}
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
							getMultiStore().renameLocation((Location) event.getData(), elem.getLocation());
							ICardStore<IMagicCard> store = getStore(elem.getLocation());
							DataManager.getInstance().update(store, Collections.singleton(MagicCardField.LOCATION));
						}
						break;
					}
					case CardEvent.UPDATE_CONTAINER: {
						break;
					}
					default:
						break;
					}
					return;
				}
			}.start();
		}
	}

	public CollectionMultiFileCardStore getMultiStore() {
		return this;
	}
}
