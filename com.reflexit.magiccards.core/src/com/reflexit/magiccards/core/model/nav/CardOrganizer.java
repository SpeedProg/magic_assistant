package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.events.CardEvent;

public class CardOrganizer extends CardElement {
	private final Collection<CardElement> children = new ArrayList<CardElement>();

	public CardOrganizer(String filename, CardOrganizer parent) {
		this(nameFromFile(filename), parent == null ? new Path(filename) : parent.getPath().append(filename), parent);
	}

	public CardOrganizer(String name, IPath path, CardOrganizer parent) {
		super(name, path);
		createDir();
		setParentInit(parent);
	}

	private void createDir() {
		try {
			File file = getFile();
			if (!file.exists()) {
				if (file.mkdir() == false)
					throw new IOException("Directory name " + file + " is invalid");
			}
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

	public IContainer getContainer() {
		return (IContainer) getResource();
	}

	public void create() throws CoreException {
		IProject project = DataManager.getProject();
		IFolder dir = project.getFolder(getPath());
		if (!dir.exists())
			dir.create(IResource.NONE, true, null);
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

	/* (non-Javadoc)
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
			try {
				if (el.getFile().getName().equals(name)) {
					return el;
				}
			} catch (CoreException e) {
				continue;
			}
		}
		return null;
	}

	/**
	 * @param path
	 * @return
	 */
	public CardElement findElement(IPath p) {
		if (p.isRoot())
			return this;
		String top = p.removeLastSegments(p.segmentCount() - 1).toString();
		IPath rest = p.removeFirstSegments(1);
		for (Object element : getChildren()) {
			CardElement el = (CardElement) element;
			if (el.getPath().equals(p))
				return el;
			if (top.equals(el.getName())) {
				if (el instanceof CardOrganizer) {
					if (rest == null || rest.isEmpty())
						return el;
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
