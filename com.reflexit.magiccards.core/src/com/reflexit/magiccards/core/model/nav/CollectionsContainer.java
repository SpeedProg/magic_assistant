package com.reflexit.magiccards.core.model.nav;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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

	public CollectionsContainer(LocationPath path, CardOrganizer parent) {
		super(path, parent);
	}

	@SuppressWarnings("unused")
	public void loadChildren() {
		File dir = getFile();
		for (File mem : dir.listFiles()) {
			if (!mem.exists())
				continue;
			String name = mem.getName();
			// System.err.println(this + "/" + name);
			if (name.equals("MagicDB"))
				continue; // skip this ones
			if (name.startsWith("."))
				continue; // skip this one too
			CardElement el = findChieldByName(name);
			if (mem.isDirectory()) {
				if (el == null) {
					CollectionsContainer con = new CollectionsContainer(name, this);
					con.loadChildren();
				} else {
					if (el instanceof CollectionsContainer) {
						((CollectionsContainer) el).loadChildren();
					}
				}
			} else {
				if (name.endsWith(".xml")) {
					if (el == null) {
						boolean deck = checkType(mem);
						new CardCollection(name, this, deck);
					}
				}
			}
		}
	}

	private boolean checkType(File mem) {
		try {
			byte[] headerBytes = new byte[1000];
			InputStream openStream = new FileInputStream(mem);
			try {
				int k = openStream.read(headerBytes);
				if (k == -1)
					return false;
				String header = new String(headerBytes, 0, k);
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
