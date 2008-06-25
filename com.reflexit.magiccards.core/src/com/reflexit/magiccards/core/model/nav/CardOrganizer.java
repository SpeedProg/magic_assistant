package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import java.util.ArrayList;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.events.CardEvent;

public class CardOrganizer extends CardElement {
	private java.util.Collection<CardElement> children = new ArrayList<CardElement>();

	public CardOrganizer(String string, CardOrganizer parent) {
		super(string, parent);
	}

	public java.util.Collection<CardElement> getChildren() {
		return this.children;
	}

	public void addChild(CardElement a) {
		this.children.add(a);
		try {
			getResource().refreshLocal(1, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fireEvent(new CardEvent(this, CardEvent.ADD_CONTAINER));
	}

	public boolean hasChildren() {
		return this.children.size() > 0;
	}

	/**
	 * @param el
	 */
	public void removeChild(CardElement el) {
		this.children.remove(el);
		IPath p = el.getPath().removeFirstSegments(0);
		try {
			IResource mem = DataManager.getProject().findMember(p);
			if (mem != null)
				mem.delete(true, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fireEvent(new CardEvent(el, CardEvent.REMOVE_CONTAINER));
	}
}
