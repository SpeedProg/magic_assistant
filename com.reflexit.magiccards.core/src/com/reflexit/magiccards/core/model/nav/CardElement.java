package com.reflexit.magiccards.core.model.nav;

import java.io.File;
import java.io.IOException;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.EventManager;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.ILocatable;

/**
 * This is base class that describe card containers. It basically either Deck or
 * Deck Folder.
 * 
 * @author Alena
 * 
 */
public abstract class CardElement extends EventManager implements ILocatable {
	private String name; // name
	private LocationPath path; // project relative path
	private CardOrganizer parent;

	public CardElement(String name, LocationPath path) {
		this.name = name;
		this.path = path;
	}

	protected void setParentInit(CardOrganizer parent) {
		setParent(parent);
		if (parent != null) {
			parent.addChild(this);
		}
	}

	protected void setParent(CardOrganizer parent) {
		this.parent = parent;
		if (parent != null) {
			if (!parent.isRoot())
				this.path = parent.getPath().append(path.lastSegment());
		}
	}

	public CardElement(String filename, CardOrganizer parent, boolean addToParent) {
		this(nameFromFile(filename), parent == null ? new LocationPath(filename) : parent.getPath().append(filename));
		if (addToParent)
			setParentInit(parent);
	}

	public LocationPath getPath() {
		return this.path;
	}

	@Override
	public Location getLocation() {
		return Location.createLocation(getPath());
	}

	/**
	 * @return
	 */
	public File getFile() {
		LocationPath p = getPath();
		if (p == null)
			return null;
		return new File(getRootDir(), p.toString());
	}

	public File getRootDir() {
		if (!isRoot())
			return getParent().getRootDir();
		return null; // this will be overriden in root
	}

	public String getName() {
		return this.name;
	}

	public CardOrganizer getParent() {
		return this.parent;
	}

	public void addListener(ICardEventListener lis) {
		addListenerObject(lis);
	}

	public void removeListener(ICardEventListener lis) {
		removeListenerObject(lis);
	}

	protected void fireEvent(int type, Object data) {
		if (isListenerAttachedRecurse()) {
			fireEvent(new CardEvent(this, type, data));
		}
	}

	public boolean isListenerAttachedRecurse() {
		if (isListenerAttached())
			return true;
		if (parent != null)
			return parent.isListenerAttachedRecurse();
		return false;
	}

	protected void fireEvent(CardEvent event) {
		Object[] listeners = getListeners();
		for (Object listener : listeners) {
			ICardEventListener lis = (ICardEventListener) listener;
			try {
				lis.handleEvent(event);
			} catch (Throwable t) {
				MagicLogger.log(t);
			}
		}
		if (this.parent != null)
			this.parent.fireEvent(event);
	}

	public void remove() {
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		if (!getFile().delete()) {
			MagicLogger.log(new IOException("Cannot delete " + getFile()));
		}
	}

	public static String nameFromFile(String filename) {
		String lastSegment = new File(filename).getName();
		int index = lastSegment.lastIndexOf('.');
		if (index == -1) {
			return lastSegment;
		}
		return lastSegment.substring(0, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @param value
	 * @return
	 */
	public CardElement rename(String value) {
		Location oldName = getLocation();
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		File newFile = new File(parent.getFile(), value + ".xml");
		getFile().renameTo(newFile);
		CardElement x = newElement(value, getParent());
		x.fireEvent(CardEvent.RENAME_CONTAINER, oldName);
		return x;
	}

	public void update() {
		fireEvent(CardEvent.UPDATE_CONTAINER, null);
		return;
	}

	public CardElement newParent(CardOrganizer parent) {
		if (parent.isAncestor(this) || this.equals(parent)) {
			throw new MagicException("Cannot move inside itself");
		}
		Location oldName = getLocation();
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		File newFile = new File(parent.getFile(), getFile().getName());
		getFile().renameTo(newFile);
		setParentInit(parent);
		fireRecorsiveRename(this, oldName);
		return this;
	}

	public boolean isAncestor(CardElement parent) {
		if (parent == null || !(parent instanceof CardOrganizer) || getParent() == null)
			return false;
		if (parent.equals(getParent()))
			return true;
		return getParent().isAncestor(parent);
	}

	private void fireRecorsiveRename(CardElement cardElement, Location oldName) {
		cardElement.setParent(cardElement.getParent()); // that would update
														// path
		if (cardElement instanceof CardOrganizer) {
			CardOrganizer org = (CardOrganizer) this;
			for (CardElement el : org.getChildren()) {
				fireRecorsiveRename(el, oldName.append(el.getName()));
			}
		}
		cardElement.fireEvent(CardEvent.RENAME_CONTAINER, oldName);
	}

	public abstract CardElement newElement(String name, CardOrganizer parent);

	public boolean isRoot() {
		return false;
	}

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException("setLocation is not supporterd");
	}

	public CardElement getRelated() {
		Location loc = getLocation();
		if (loc.isSideboard())
			return getParent().findChieldByName(loc.toMainDeck().getBaseFileName());
		return getParent().findChieldByName(loc.toSideboard().getBaseFileName());
	}
}
