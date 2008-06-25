package com.reflexit.magiccards.core.xml;

import org.eclipse.core.runtime.CoreException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.thoughtworks.xstream.XStream;

public class MultiFileCardStore extends AbstractCardStore<IMagicCard> {
	protected HashMap<String, SubTable> map;
	protected int size;

	public MultiFileCardStore() {
		this.map = new HashMap<String, SubTable>();
	}

	public synchronized void addFile(File file) {
		SubTable table = new SubTable();
		table.key = file.getName();
		table.file = file;
		table.list = new ArrayList<IMagicCard>();
		this.map.put(table.key, table);
		this.initialized = false;
	}

	@Override
	protected synchronized void doInitialize() {
		XStream xstream = DataManager.getXStream();
		xstream.setClassLoader(this.getClass().getClassLoader());
		ArrayList<SubTable> all = new ArrayList<SubTable>();
		all.addAll(this.map.values());
		this.map.clear();
		for (Iterator<SubTable> iterator = all.iterator(); iterator.hasNext();) {
			SubTable table = iterator.next();
			try {
				FileInputStream is = new FileInputStream(table.file);
				SubTable loaded = (SubTable) xstream.fromXML(is);
				is.close();
				this.size += table.list.size();
				this.map.put(loaded.key, loaded);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Activator.log(e);
				this.map.put(table.key, table);
			}
		}
	}

	public Collection<IMagicCard> filterCards(MagicCardFilter filter) throws MagicException {
		initialize();
		Comparator<IMagicCard> comp = MagicCardComparator.getComparator(filter.getSortIndex(), filter.isAscending());
		TreeSet<IMagicCard> filteredList = new TreeSet<IMagicCard>(comp);
		for (Iterator<IMagicCard> iterator = cardsIterator(); iterator.hasNext();) {
			IMagicCard elem = iterator.next();
			if (!filter.isFiltered(elem)) {
				filteredList.add(elem);
			}
		}
		return filteredList;
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
			res.file = getFile(key);
			this.map.put(key, res);
		}
		return res.list.add(card);
	}

	private File getFile(String key) {
		try {
			if (key.equals("library")) {
				return XmlCardHolder.getLibrary();
			} else {
				key = key.replaceAll("[\\W]", "_");
				return new File(XmlCardHolder.getDbFolder(), key + ".xml");
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	protected String getKey(IMagicCard card) {
		if (card.getClass() == MagicCard.class) {
			return (card).getEdition();
		} else {
			return "library";
		}
	}

	public void save() {
		try {
			doSave();
		} catch (FileNotFoundException e) {
			throw new MagicException(e);
		}
	}

	protected synchronized void doSave() throws FileNotFoundException {
		XStream xstream = DataManager.getXStream();
		for (Iterator iterator = this.map.values().iterator(); iterator.hasNext();) {
			SubTable table = (SubTable) iterator.next();
			OutputStream out = new FileOutputStream(table.file);
			xstream.toXML(table, out);
		}
	}
}
