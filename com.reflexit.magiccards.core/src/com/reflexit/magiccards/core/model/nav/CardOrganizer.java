package com.reflexit.magiccards.core.model.nav;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.events.CardEvent;

public class CardOrganizer extends CardElement {
	private Collection<CardElement> children = new ArrayList<CardElement>();

	public CardOrganizer(String filename, CardOrganizer parent) {
		super(filename, parent);
	}

	public CardOrganizer(String name, IPath path, CardOrganizer parent) {
		super(name, path, parent);
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

	public boolean contains(String name) {
		for (CardElement el : getChildren()) {
			try {
				if (el.getFile().getName().equals(name)) {
					return true;
				}
			} catch (CoreException e) {
				continue;
			}
		}
		return false;
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
			if (top.equals(el.getName()) && el instanceof CardOrganizer) {
				if (rest == null || rest.isEmpty())
					return el;
				return ((CardOrganizer) el).findElement(rest);
			}
		}
		return null;
	}

	@Override
	public CardElement newElement(String name, CardOrganizer parent) {
		return new CardOrganizer(name + ".xml", parent);
	}
}
