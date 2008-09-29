package com.reflexit.magiccards.core.xml;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class MultiFileCardStore extends AbstractCardStore<IMagicCard> {
	protected HashMap<String, SubTable> map;
	protected int size;

	public MultiFileCardStore() {
		this.map = new HashMap<String, SubTable>();
	}

	public synchronized void addFile(File file, String key) {
		SubTable table = new SubTable();
		table.key = key;
		table.file = file;
		table.list = new ArrayList<IMagicCard>();
		this.map.put(table.key, table);
		this.initialized = false;
	}

	@Override
	protected synchronized void doInitialize() {
		ArrayList<SubTable> all = new ArrayList<SubTable>();
		all.addAll(this.map.values());
		this.map.clear();
		for (Iterator<SubTable> iterator = all.iterator(); iterator.hasNext();) {
			SubTable table = iterator.next();
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
	private void setLocations(String key, Collection list) {
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
				if (!this.cur.hasNext()) {
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

	@Override
	protected synchronized void doAddAll(Collection col) {
		for (Iterator iterator = col.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			doAddCard(card);
		}
	}

	@Override
	public void doRemoveCard(IMagicCard card) {
		String key = getKey(card);
		SubTable res = this.map.get(key);
		if (res != null) {
			res.list.remove(card);
		}
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
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

	private File getFile(IMagicCard card) {
		try {
			String key = getKey(card);
			if (card instanceof MagicCard) {
				key = key.replaceAll("[\\W]", "_");
				return new File(XmlCardHolder.getDbFolder(), key + ".xml");
			} else if (card instanceof MagicCardPhisical) {
				String file = key;
				IResource res = DataManager.getProject().findMember(new Path(file));
				return res.getLocation().toFile();
			} else
				throw new MagicException("Unknown card type");
		} catch (CoreException e) {
			throw new MagicException("Can't resolve file: ", e);
		}
	}

	protected String getKey(IMagicCard card) {
		if (card instanceof MagicCard) {
			return (card).getEdition();
		} else if (card instanceof MagicCardPhisical) {
			MagicCardPhisical mp = (MagicCardPhisical) card;
			String loc = mp.getLocation();
			if (loc == null) {
				loc = DataManager.getModelRoot().getDefaultLib().getLocation();
				mp.setLocation(loc);
			}
			return loc;
		}
		return "unknown";
	}

	public void save() {
		try {
			doSave();
		} catch (FileNotFoundException e) {
			throw new MagicException(e);
		}
	}

	protected synchronized void doSave() throws FileNotFoundException {
		for (Iterator iterator = this.map.values().iterator(); iterator.hasNext();) {
			SubTable table = (SubTable) iterator.next();
			CardCollectionStoreObject obj = table.toCardCollectionStoreObject();
			obj.save();
		}
	}
}
