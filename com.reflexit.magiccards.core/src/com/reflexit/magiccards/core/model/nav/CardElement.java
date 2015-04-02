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
	private LocationPath path; // project relative path
	private CardOrganizer parent;

	public CardElement(LocationPath path) {
		this.path = path;
	}

	public CardElement(String filename, CardOrganizer parent, boolean addToParent) {
		this(parent == null ? new LocationPath(filename) : parent.getPath().append(
				filename));
		if (addToParent) {
			setParentInit(parent);
			if (parent != null) parent.fireCreationEvent(this);
		}
	}

	protected void setParentInit(CardOrganizer parent) {
		setParent(parent);
		if (parent != null) {
			parent.doAddChild(this);
		}
	}

	protected void setParent(CardOrganizer parent) {
		this.parent = parent;
		if (parent != null && !parent.isRoot()) {
			String lastSegment = path.lastSegment();
			this.path = parent.getPath().append(lastSegment);
		}
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
		return path.getBaseName();
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
		File file = getFile();
		if (file.exists() && !file.delete()) {
			MagicLogger.log(new IOException("Cannot delete " + file));
		}
	}

	public static String basename(String filename) {
		String lastSegment = new File(filename).getName();
		int index = lastSegment.lastIndexOf('.');
		if (index == -1) {
			return lastSegment;
		}
		return lastSegment.substring(0, index);
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getLabel() {
		return getName();
	}

	/**
	 * @param value
	 * @return
	 */
	public CardElement rename(String value) {
		Location oldName = getLocation();
		String ext = path.getFileExtensionWithDot();
		String lastSegment = value + ext;
		File oldFile = getFile();
		path = parent.getPath().append(lastSegment);
		File newFile = getFile();
		oldFile.renameTo(newFile);
		fireRecursiveRename(this, oldName);
		return this;
	}

	public void update() {
		fireEvent(CardEvent.UPDATE_CONTAINER, this);
		return;
	}

	public CardElement newParent(CardOrganizer parent) {
		if (parent.isAncestor(this) || this.equals(parent)) {
			throw new MagicException(
					"Cannot move inside itself");
		}
		Location oldName = getLocation();
		if (getParent() != null) {
			getParent().doRemoveChild(this);
		}
		File newFile = new File(parent.getFile(), getFile().getName());
		getFile().renameTo(newFile);
		setParentInit(parent);
		fireRecursiveRename(this, oldName);
		return this;
	}

	public boolean isAncestor(CardElement parent) {
		if (parent == null || !(parent instanceof CardOrganizer) || getParent() == null)
			return false;
		if (parent.equals(getParent()))
			return true;
		return getParent().isAncestor(parent);
	}

	private void fireRecursiveRename(CardElement cardElement, Location oldName) {
		cardElement.setParent(cardElement.getParent()); // that would update
														// path
		if (cardElement instanceof CardOrganizer) {
			CardOrganizer org = (CardOrganizer) this;
			for (CardElement el : org.getChildren()) {
				fireRecursiveRename(el, oldName.append(el.getName()));
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
