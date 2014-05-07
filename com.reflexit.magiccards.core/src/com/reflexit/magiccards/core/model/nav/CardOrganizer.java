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
		this(nameFromFile(filename), parent == null ? new LocationPath(filename) : parent.getPath().append(filename), parent);
	}

	public CardOrganizer(String name, LocationPath path, CardOrganizer parent) {
		super(name, path);
		setParentInit(parent);
		createDir();
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
		this.children.add(a);
		fireEvent(new CardEvent(this, CardEvent.ADD_CONTAINER, a));
	}

	public void create() throws IOException {
		File dir = getFile();
		if (!dir.exists()) {
			if (!dir.mkdir()) {
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
		this.children.remove(el);
		fireEvent(new CardEvent(this, CardEvent.REMOVE_CONTAINER, el));
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
	public Collection<CardElement> getAllElements() {
		ArrayList<CardElement> res = new ArrayList<CardElement>();
		for (CardElement el : getChildren()) {
			if (el instanceof CardOrganizer) {
				res.addAll(((CardOrganizer) el).getAllElements());
			} else {
				res.add(el);
			}
		}
		return res;
	}

	public boolean contains(Location loc) {
		return contains(loc.getBaseFileName());
	}

	public boolean contains(String name) {
		return findChieldByName(name) != null;
	}

	public CardElement findChieldByName(String name) {
		for (CardElement el : getChildren()) {
			if (el.getFile().getName().equals(name)) {
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
		String[] parts = p.splitTop();
		String top = parts[0];
		LocationPath rest = new LocationPath(parts[1]);
		for (Object element : getChildren()) {
			CardElement el = (CardElement) element;
			if (el.getPath().equals(p))
				return el;
			if (top.equals(el.getName())) {
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
		for (Object element : this.getChildren()) {
			CardElement o = (CardElement) element;
			if (o instanceof CardCollection) {
				CardCollection d = (CardCollection) o;
				if (d.getFileName().equals(id))
					return d;
			} else if (o instanceof CardOrganizer) {
				CardCollection d = ((CardOrganizer) o).findCardCollectionById(id);
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