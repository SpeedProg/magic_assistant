package com.reflexit.magiccards.core.model.nav;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.events.CardEvent;

/**
 * This represent a folder where decks or collections are stored (or any sort of super container)
 *
 * @author Alena
 *
 */
public class CardOrganizer extends CardElement {
	private final Collection<CardElement> children = new ArrayList<CardElement>();

	public CardOrganizer(String filename, CardOrganizer parent) {
		this(parent == null ? new LocationPath(filename) : parent.getPath().append(
				filename), parent);
	}

	public CardOrganizer(LocationPath path, CardOrganizer parent) {
		super(path);
		setParentInit(parent);
		createDir();
		if (parent != null) parent.fireCreationEvent(this);
	}

	private void createDir() {
		try {
			if (!isRoot())
				create();
		} catch (Exception e) {
			throw new MagicException(e);
		}
	}

	public Collection<CardElement> getChildren() {
		return this.children;
	}

	public void addChild(CardElement a) {
		doAddChild(a);
		fireCreationEvent(a);
	}

	void fireCreationEvent(CardElement a) {
		fireEvent(new CardEvent(this, CardEvent.ADD_CONTAINER, a));
	}

	void doAddChild(CardElement a) {
		this.children.add(a);
	}

	public void create() throws IOException {
		File dir = getFile();
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("Cannot create " + dir);
			}
		}
	}

	public boolean hasChildren() {
		return this.children.size() > 0;
	}

	/**
	 * @param el
	 */
	public void removeChild(CardElement el) {
		doRemoveChild(el);
		fireEvent(new CardEvent(this, CardEvent.REMOVE_CONTAINER, el));
	}

	void doRemoveChild(CardElement el) {
		this.children.remove(el);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.core.model.nav.CardElement#remove()
	 */
	@Override
	public void remove() {
		removeChildren();
		super.remove();
	}

	public void removeChildren() {
		Collection children2 = new ArrayList(getChildren());
		for (Iterator iterator = children2.iterator(); iterator.hasNext();) {
			CardElement el = (CardElement) iterator.next();
			el.remove();
		}
	}

	/**
	 * @return
	 */
	public Collection<CardCollection> getAllElements() {
		ArrayList<CardCollection> res = new ArrayList<CardCollection>();
		for (CardElement el : getChildren()) {
			if (el instanceof CardOrganizer) {
				res.addAll(((CardOrganizer) el).getAllElements());
			} else if (el instanceof CardCollection) {
				res.add((CardCollection) el);
			}
		}
		return res;
	}

	public boolean contains(Location loc) {
		return contains(loc.getBaseFileName());
	}

	private boolean contains(String name) {
		return findChieldByName(name) != null;
	}

	public CardElement findChieldByName(String name) {
		for (CardElement el : getChildren()) {
			String baseFileName = el.getFile().getName();
			if (baseFileName.equals(name)) {
				return el;
			}
		}
		return null;
	}

	public CardElement findElement(String p) {
		return findElement(new LocationPath(p));
	}

	/**
	 * @param path
	 * @return
	 */
	public CardElement findElement(LocationPath p) {
		if (p.isRoot())
			return this;
		String top = p.getHead();
		for (Object element : getChildren()) {
			CardElement el = (CardElement) element;
			if (el.getPath().equals(p))
				return el;
			if (top.equals(el.getName())) {
				LocationPath rest = p.getTail();
				if (rest.isEmpty())
					return el;
				if (el instanceof CardOrganizer) {
					return ((CardOrganizer) el).findElement(rest);
				}
			}
		}
		for (Object element : getChildren()) {
			CardElement el = (CardElement) element;
			if (el instanceof CardOrganizer) {
				return ((CardOrganizer) el).findElement(p);
			}
		}
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public CardCollection findCardCollectionById(String id) {
		CardElement el = findElement(id);
		if (el instanceof CardCollection) {
			return (CardCollection) el;
		}
		// backward compatibity
		return oldFindCardCollectionById(id);
	}

	public CardCollection oldFindCardCollectionById(String id) {
		String fileId = id + ".xml";
		for (Object element : this.getChildren()) {
			CardElement o = (CardElement) element;
			if (o instanceof CardCollection) {
				CardCollection d = (CardCollection) o;
				if (d.getPath().getName().equals(id)) // old Id is file base name with ext
					return d;
				if (d.getPath().getName().equals(fileId))
					return d;
			} else if (o instanceof CardOrganizer) {
				CardCollection d = ((CardOrganizer) o).oldFindCardCollectionById(id);
				if (d != null)
					return d;
			}
		}
		return null;
	}

	@Override
	public CardElement newElement(String name, CardOrganizer parent) {
		return new CardOrganizer(name + ".xml", parent);
	}
}
