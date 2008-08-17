package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import java.util.Iterator;

public class DecksContainer extends CardOrganizer {
	public DecksContainer(String name, CardOrganizer parent) {
		super(name, parent);
	}

	public void loadChildren() throws CoreException {
		IResource[] members = getContainer().members();
		for (int i = 0; i < members.length; i++) {
			IResource mem = members[i];
			String name = mem.getName();
			if (mem instanceof IContainer) {
				DecksContainer con = new DecksContainer(name, this);
				con.loadChildren();
			} else if (mem != null) {
				if (name.endsWith(".xml")) {
					if (!this.contains(name))
						new Deck(name, this);
				}
			}
		}
	}

	public Deck addDeck(String name) {
		Deck d = new Deck(name, this);
		return d;
	}

	public DecksContainer addDeckContainer(String name) {
		DecksContainer d = new DecksContainer(name, this);
		return d;
	}

	/**
	 * @param id
	 * @return
	 */
	public Deck findDeck(String id) {
		for (Iterator iterator = this.getChildren().iterator(); iterator.hasNext();) {
			CardElement o = (CardElement) iterator.next();
			if (o instanceof Deck) {
				Deck d = (Deck) o;
				if (d.getFileName().equals(id))
					return d;
			} else if (o instanceof DecksContainer) {
				Deck d = ((DecksContainer) o).findDeck(id);
				if (d != null)
					return d;
			}
		}
		return null;
	}

	/**
	 * @param el
	 */
	public void removeDeck(Deck el) {
		el.remove();
	}
}
