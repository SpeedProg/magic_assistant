package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.AbstractCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.thoughtworks.xstream.XStream;

public class SingleFileCardStore extends AbstractCardStore<IMagicCard> {
	protected ArrayList<IMagicCard> list;
	protected File file;

	public SingleFileCardStore(File file) {
		this.list = null;
		this.file = file;
	}

	@Override
	protected synchronized void doInitialize() {
		if (this.file.exists()) {
			XStream xstream = DataManager.getXStream();
			xstream.setClassLoader(this.getClass().getClassLoader());
			try {
				FileInputStream is = new FileInputStream(this.file);
				SingleFileCardStore loaded = (SingleFileCardStore) xstream.fromXML(is);
				is.close();
				this.list = loaded.list;
			} catch (Exception e) {
				System.err.println("Cannot load file " + this.file);
				e.printStackTrace();
				Activator.log(e);
				this.list = new ArrayList<IMagicCard>();
			}
		} else {
			try {
				new FileOutputStream(this.file).close();
			} catch (IOException e) {
				// ignore
			}
			this.list = new ArrayList<IMagicCard>();
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
		return this.list.iterator();
	}

	@Override
	public void doRemoveCard(IMagicCard card) {
		this.list.remove(card);
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		return this.list.add(card);
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
		OutputStream out = new FileOutputStream(this.file);
		xstream.toXML(this, out);
	}

	@Override
	protected void doAddAll(Collection cards) {
		this.list.addAll(cards);
	}

	public int getTotal() {
		return this.list.size();
	}
}
