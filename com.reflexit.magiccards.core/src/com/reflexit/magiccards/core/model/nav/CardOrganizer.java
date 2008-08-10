package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import java.util.ArrayList;
import java.util.Collection;

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
		a.setParent(this);
		fireEvent(new CardEvent(this, CardEvent.ADD_CONTAINER));
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
		fireEvent(new CardEvent(el, CardEvent.REMOVE_CONTAINER));
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
}
