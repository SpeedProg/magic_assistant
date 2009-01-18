package com.reflexit.magiccards.core.xml;

import org.eclipse.core.runtime.CoreException;

import java.util.Collection;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter.BinaryExpr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Expr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Node;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;

public class LibraryDataXmlHandler extends AbstractFilteredCardStore<IMagicCard> implements ILocatable,
        ICardEventListener, ICardCountable, IStorageContainer<IMagicCard> {
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
		this.table.clear();
		for (CardElement elem : colls) {
			try {
				this.table.addFile(elem.getFile(), elem.getLocation());
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		this.table.setLocation(def.getLocation());
		this.table.initialize();
		container.addListener(this);
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

	public String getLocation() {
		Expr root = getFilter().getRoot();
		String loc = findLocationFilter(root);
		if (loc != null)
			return loc;
		return table.getLocation();
	}

	private String findLocationFilter(Expr root) {
		if (root instanceof BinaryExpr) {
			BinaryExpr bin = ((BinaryExpr) root);
			if (bin.getLeft() instanceof Node && ((Node) bin.getLeft()).toString().equals("location")) {
				return bin.getRight().toString();
			}
			String loc = findLocationFilter(bin.getLeft());
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
		if (event.getData() instanceof CardCollection) {
			CardCollection elem = (CardCollection) event.getData();
			if (event.getType() == CardEvent.ADD_CONTAINER) {
				try {
					this.table.addFile(elem.getFile(), elem.getLocation());
				} catch (CoreException e) {
					Activator.log(e);
				}
				reload();
			} else if (event.getType() == CardEvent.REMOVE_CONTAINER) {
				this.table.removeFile(elem.getLocation());
				reload();
			}
		} else if (event.getSource() instanceof CardCollection) {
			CardCollection elem = (CardCollection) event.getSource();
			if (event.getType() == CardEvent.RENAME_CONTAINER) {
				this.table.renameLocation((String) event.getData(), elem.getLocation());
				reload();
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

	public IStorage<IMagicCard> getStorage() {
		return table.getStorage();
	}

	public SingleFileCardStorage getStorage(String location) {
		initialize();
		if (location == null)
			return table.getMStorage().getStorage(table.getMStorage().getLocation());
		return table.getMStorage().getStorage(location);
	}
}
