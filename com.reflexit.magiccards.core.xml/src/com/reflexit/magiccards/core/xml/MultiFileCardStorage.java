package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.AbstractStorage;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.IStorage;

public class MultiFileCardStorage extends AbstractStorage<IMagicCard> implements ILocatable, IStorage<IMagicCard> {
	protected HashMap<String, SingleFileCardStorage> map;
	protected int size;
	protected String defaultLocation;

	public MultiFileCardStorage() {
		this.map = new HashMap<String, SingleFileCardStorage>();
	}

	public synchronized void addFile(final File file, final String location, boolean initialize) {
		SingleFileCardStorage table = new SingleFileCardStorage(file, location, initialize);
		this.map.put(table.getLocation(), table);
		this.size += table.size();
	}

	public File getFile(String location) {
		SingleFileCardStorage loc = map.get(location);
		if (loc != null)
			return loc.file;
		return null;
	}

	public synchronized void removeFile(String location) {
		this.map.remove(location);
	}

	@Override
	public synchronized void doLoad() {
		ArrayList<SingleFileCardStorage> all = new ArrayList<SingleFileCardStorage>();
		all.addAll(this.map.values());
		for (SingleFileCardStorage table : all) {
			try {
				table.load();
				this.size += table.size();
			} catch (Exception e) {
				e.printStackTrace();
				Activator.log(e);
			}
			this.map.put(table.getLocation(), table);
		}
	}

	public Iterator<IMagicCard> iterator() {
		final Iterator<SingleFileCardStorage> iter = this.map.values().iterator();
		return new Iterator<IMagicCard>() {
			Iterator<IMagicCard> cur;
			{
				if (iter.hasNext())
					this.cur = (iter.next()).iterator();
				else
					this.cur = null;
			}

			public boolean hasNext() {
				checkNext();
				return this.cur != null && this.cur.hasNext();
			}

			void checkNext() {
				if (this.cur == null)
					return;
				while (cur != null && !this.cur.hasNext()) {
					//if (!this.cur.hasNext()) {
					if (iter.hasNext()) {
						this.cur = (iter.next()).iterator();
					} else {
						this.cur = null;
					}
				}
			}

			public IMagicCard next() {
				checkNext();
				if (this.cur == null)
					throw new NoSuchElementException();
				return this.cur.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public int size() {
		getDeepSize();
		return this.size;
	}

	public int getDeepSize() {
		int s = 0;
		for (Object element : this) {
			s++;
		}
		if (size != s)
			System.err.println("Size mismatch: " + s + " " + size);
		return s;
	}

	@Override
	public synchronized boolean doRemoveCard(final IMagicCard card) {
		String key = getKey(card);
		SingleFileCardStorage res = this.map.get(key);
		if (res != null) {
			int oldSize = res.size();
			boolean modified = res.doRemoveCard(card);
			this.size -= oldSize - res.size();
			return modified;
		}
		return false;
	}

	@Override
	public synchronized boolean doAddCard(final IMagicCard card) {
		String key = getKey(card);
		SingleFileCardStorage res = this.map.get(key);
		if (res == null) {
			res = new SingleFileCardStorage(getFile(card), key);
			this.map.put(key, res);
		}
		this.size -= res.size();
		boolean modified = res.doAddCard(card);
		this.size += res.size();
		return modified;
	}

	private File getFile(final IMagicCard card) {
		try {
			String key = getKey(card);
			if (card instanceof MagicCard) {
				return new File(XmlCardHolder.getDbFolder(), key + ".xml");
			} else if (card instanceof MagicCardPhisical) {
				SingleFileCardStorage subTable = this.map.get(key);
				if (subTable == null)
					throw new MagicException("Invalid Key: " + key);
				return subTable.file;
			} else
				throw new MagicException("Unknown card type");
		} catch (CoreException e) {
			throw new MagicException("Can't resolve file: ", e);
		}
	}

	protected String getKey(final IMagicCard card) {
		if (card instanceof MagicCard) {
			String key = (card).getEdition();
			key = key.replaceAll("[\\W]", "_");
			return key;
		} else if (card instanceof MagicCardPhisical) {
			MagicCardPhisical mp = (MagicCardPhisical) card;
			String loc = mp.getLocation();
			if (loc == null) {
				loc = this.defaultLocation;
				mp.setLocation(loc);
			}
			return loc;
		}
		return "unknown";
	}

	@Override
	protected synchronized void doSave() throws FileNotFoundException {
		for (SingleFileCardStorage table : this.map.values()) {
			table.save();
		}
	}

	/**
	 * @param location
	 */
	public void setLocation(final String location) {
		if (map.size() > 0) {
			if (map.get(location) == null)
				throw new IllegalArgumentException("key is invalid");
		}
		this.defaultLocation = location;
	}

	public String getLocation() {
		return defaultLocation;
	}

	public void renameLocation(String oldLocation, String newLocation) {
		SingleFileCardStorage loaded = map.get(oldLocation);
		loaded.setLocation(newLocation);
		loaded.updateLocations();
		map.remove(oldLocation);
		map.put(newLocation, loaded);
		save();
	}

	@Override
	public boolean removeAll() {
		boolean modified = false;
		for (SingleFileCardStorage table : map.values()) {
			if (table.removeAll()) {
				modified = true;
			}
		}
		size = 0;
		return modified;
	}

	@Override
	public void clearCache() {
		size = 0;
		for (SingleFileCardStorage table : this.map.values()) {
			table.clearCache();
		}
	}
}
