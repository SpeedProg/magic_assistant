package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class DecksContainer extends CardOrganizer {
	public DecksContainer(String name, CardOrganizer parent) {
		super(name, parent);
	}

	public void loadChildren() throws CoreException {
		IResource[] members = getContainer().members();
		for (IResource mem : members) {
			String name = mem.getName();
			if (mem instanceof IContainer) {
				DecksContainer con = new DecksContainer(name, this);
				con.loadChildren();
			} else if (mem != null) {
				if (name.endsWith(".xml")) {
					if (!this.contains(name))
						new CardCollection(name, this, true);
				}
			}
		}
	}

	public CardCollection addDeck(String name) {
		CardCollection d = new CardCollection(name, this, true);
		return d;
	}

	public DecksContainer addDeckContainer(String name) {
		DecksContainer d = new DecksContainer(name, this);
		return d;
	}

	/**
	 * @param el
	 */
	public void removeDeck(CardCollection el) {
		el.remove();
	}

	@Override
	public CardElement newElement(String name, CardOrganizer parent) {
		return new DecksContainer(name + ".xml", parent);
	}
}
