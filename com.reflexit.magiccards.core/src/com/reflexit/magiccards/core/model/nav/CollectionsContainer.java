package com.reflexit.magiccards.core.model.nav;

import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.reflexit.magiccards.core.model.Location;

/**
 * Specific organizer for decks and collections
 * 
 * @author Alena
 * 
 */
public class CollectionsContainer extends CardOrganizer {
	public CollectionsContainer(String name, CardOrganizer parent) {
		super(name, parent);
	}

	public CollectionsContainer(String name, IPath path, CardOrganizer parent) {
		super(name, path, parent);
	}

	public void loadChildren() throws CoreException {
		getContainer().refreshLocal(IResource.DEPTH_ONE, null);
		IResource[] members = getContainer().members();
		for (IResource mem : members) {
			if (!mem.exists() || mem.isPhantom())
				continue;
			String name = mem.getName();
			// System.err.println(this + "/" + name);
			if (name.equals("MagicDB"))
				continue; // skip this ones
			CardElement el = findChieldByName(name);
			if (mem instanceof IContainer) {
				if (el == null) {
					CollectionsContainer con = new CollectionsContainer(name, this);
					con.loadChildren();
				} else {
					if (el instanceof CollectionsContainer) {
						((CollectionsContainer) el).loadChildren();
					}
				}
			} else if (mem != null) {
				if (name.endsWith(".xml")) {
					if (el == null) {
						boolean deck = checkType(mem);
						new CardCollection(name, this, deck);
					}
				}
			}
		}
	}

	private boolean checkType(IResource mem) {
		URI locationURI = mem.getLocationURI();
		try {
			byte[] headerBytes = new byte[1000];
			InputStream openStream = locationURI.toURL().openStream();
			try {
				openStream.read(headerBytes);
				String header = new String(headerBytes);
				if (header.contains("<type>deck</type"))
					return true;
			} finally {
				openStream.close();
			}
		} catch (Exception e) {
			// skip
		}
		return false;
	}

	public CollectionsContainer addCollectionsContainer(String name) {
		CollectionsContainer d = new CollectionsContainer(name, this);
		return d;
	}

	public CardCollection addDeck(String name) {
		CardCollection d = new CardCollection(name, this, true);
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
		return new CollectionsContainer(name + ".xml", parent);
	}

	public CardElement findChield(Location loc) {
		return findChieldByName(loc.getBaseFileName());
	}
}
