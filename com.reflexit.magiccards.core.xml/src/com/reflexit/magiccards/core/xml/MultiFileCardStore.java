package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class MultiFileCardStore extends AbstractStorage<IMagicCard> implements ILocatable, IStorage<IMagicCard> {
	protected HashMap<String, SubTable> map;
	protected int size;
	protected String defKey;

	public MultiFileCardStore() {
		this.map = new HashMap<String, SubTable>();
	}

	public synchronized void addFile(final File file, final String key) {
		SubTable table = new SubTable();
		table.key = key;
		table.file = file;
		// System.err.println(key + "=" + file);
		table.list = new ArrayList<IMagicCard>();
		this.map.put(table.key, table);
		this.initialized = false;
	}

	@Override
	public synchronized void doInitialize() {
		ArrayList<SubTable> all = new ArrayList<SubTable>();
		all.addAll(this.map.values());
		this.map.clear();
		this.size = 0;
		for (SubTable table : all) {
			try {
				CardCollectionStoreObject obj = CardCollectionStoreObject.initFromFile(table.file);
				SubTable loaded = new SubTable(obj);
				this.size += loaded.list.size();
				setLocations(loaded.key, loaded.list);
				this.map.put(loaded.key, loaded);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Activator.log(e);
				this.map.put(table.key, table);
			}
		}
	}

	/**
	 * @param key
	 * @param list
	 */
	private void setLocations(final String key, final Collection list) {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof MagicCardPhisical) {
				MagicCardPhisical mp = (MagicCardPhisical) object;
				mp.setLocation(key);
			}
		}
	}

	public Iterator<IMagicCard> cardsIterator() {
		final Iterator<SubTable> iter = this.map.values().iterator();
		return new Iterator<IMagicCard>() {
			Iterator<IMagicCard> cur;
			{
				if (iter.hasNext())
					this.cur = (iter.next()).list.iterator();
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
					if (iter.hasNext()) {
						this.cur = (iter.next()).list.iterator();
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

	public int getTotal() {
		return this.size;
	}

	public int getHardTotal() {
		int s = 0;
		for (Iterator iterator = cardsIterator(); iterator.hasNext();) {
			iterator.next();
			s++;
		}
		if (size != s)
			System.err.println("Size mismatch: " + s + " " + size);
		return s;
	}

	@Override
	public synchronized boolean doRemoveCard(final IMagicCard card) {
		String key = getKey(card);
		SubTable res = this.map.get(key);
		if (res != null) {
			this.size--;
			return res.list.remove(card);
		}
		return false;
	}

	@Override
	public synchronized boolean doAddCard(final IMagicCard card) {
		String key = getKey(card);
		SubTable res = this.map.get(key);
		if (res == null) {
			res = new SubTable();
			res.list = new ArrayList<IMagicCard>();
			res.key = key;
			res.file = getFile(card);
			this.map.put(key, res);
		}
		this.size++;
		return res.list.add(card);
	}

	private File getFile(final IMagicCard card) {
		try {
			String key = getKey(card);
			if (card instanceof MagicCard) {
				return new File(XmlCardHolder.getDbFolder(), key + ".xml");
			} else if (card instanceof MagicCardPhisical) {
				SubTable subTable = this.map.get(key);
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
				loc = this.defKey;
				mp.setLocation(loc);
			}
			return loc;
		}
		return "unknown";
	}

	@Override
	protected synchronized void doSave() throws FileNotFoundException {
		for (Object element : this.map.values()) {
			SubTable table = (SubTable) element;
			CardCollectionStoreObject obj = table.toCardCollectionStoreObject();
			obj.save();
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
		this.defKey = location;
	}

	public String getLocation() {
		return defKey;
	}
}
