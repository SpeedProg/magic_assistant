package com.reflexit.magiccards.core.xml;

import org.eclipse.core.runtime.CoreException;

import java.util.Collection;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardFilter.BinaryExpr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Expr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Node;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

public class LibraryDataXmlHandler extends AbstractFilteredCardStore<IMagicCard> implements ILocatable,
        ICardEventListener, ICardCountable {
	private static LibraryDataXmlHandler instance;
	private CollectionMultiFileCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	@Override
	protected void doInitialize() throws MagicException {
		ModelRoot container = DataManager.getModelRoot();
		Collection<CardElement> colls = container.getAllElements();
		// init super
		CardCollection def = DataManager.getModelRoot().getDefaultLib();
		for (CardElement elem : colls) {
			try {
				this.table.addFile(elem.getFile(), elem.getLocation(), false);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		this.table.setLocation(def.getLocation());
		this.table.initialize();
		container.addListener(this);
		table.addListener(this);
	}

	public static LibraryDataXmlHandler getInstance() {
		if (instance == null)
			new LibraryDataXmlHandler();
		return instance;
	}

	private LibraryDataXmlHandler() {
		instance = this;
		this.table = new CollectionMultiFileCardStore();
	}

	@Override
	public Location getLocation() {
		Expr root = getFilter().getRoot();
		Location loc = findLocationFilter(root);
		if (loc != null)
			return loc;
		return table.getLocation();
	}

	private Location findLocationFilter(Expr root) {
		if (root instanceof BinaryExpr) {
			BinaryExpr bin = ((BinaryExpr) root);
			if (bin.getLeft() instanceof Node
			        && ((Node) bin.getLeft()).toString().equals(MagicCardFieldPhysical.LOCATION.name())) {
				return new Location(bin.getRight().toString());
			}
			Location loc = findLocationFilter(bin.getLeft());
			if (loc != null)
				return loc;
			loc = findLocationFilter(bin.getRight());
			if (loc != null)
				return loc;
		}
		return null;
	}

	public void setLocation(String key) {
		throw new UnsupportedOperationException("setLocation is not supported");
	}

	public void handleEvent(CardEvent event) {
		if (event.getData() instanceof CardElement) {
			CardElement elem = (CardElement) event.getData();
			if (event.getType() == CardEvent.ADD_CONTAINER) {
				try {
					if (elem instanceof CardCollection) {
						CollectionCardStore store = this.table.addFile(elem.getFile(), elem.getLocation());
						IStorage storage = store.getStorage();
						if (storage instanceof IStorageInfo) {
							((IStorageInfo) storage).setType(((CardCollection) elem).isDeck()
							        ? IStorageInfo.DECK_TYPE
							        : null);
						}
					}
				} catch (CoreException e) {
					Activator.log(e);
				}
				reload();
			} else if (event.getType() == CardEvent.REMOVE_CONTAINER) {
				this.table.removeLocation(elem.getLocation());
				reload();
			}
		} else if (event.getSource() instanceof CardElement) {
			CardElement elem = (CardElement) event.getSource();
			if (event.getType() == CardEvent.RENAME_CONTAINER) {
				this.table.renameLocation((Location) event.getData(), elem.getLocation());
				reload();
			}
		} else if (event.getType() == CardEvent.UPDATE) {
			// need to save xml
			if (event.getData() instanceof MagicCardPhisical) {
				MagicCardPhisical c = (MagicCardPhisical) event.getData();
				Location location = c.getLocation();
				AbstractCardStoreWithStorage storage = table.getStorage(location);
				if (storage != null) {
					storage.getStorage().save();
				}
			}
		}
	}

	@Override
	protected void reload() {
		this.table.setInitialized(false);
		super.reload();
		update();
	}

	public int getCount() {
		int count = 0;
		Collection<IMagicCard> list = getFilteredList();
		for (Object element : list) {
			IMagicCard magicCard = (IMagicCard) element;
			if (magicCard instanceof ICardCountable) {
				count += ((ICardCountable) magicCard).getCount();
			}
		}
		return count;
	}

	public ICardStore<IMagicCard> getStore(Location location) {
		initialize();
		if (location == null)
			return table.getStore(table.getLocation());
		return table.getStore(location);
	}
}
