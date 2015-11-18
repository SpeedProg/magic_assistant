/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.model.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardGame;
import com.reflexit.magiccards.core.model.MagicCardGame.MagicCardGameField;
import com.reflexit.magiccards.core.model.MagicCardGame.Zone;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;

/**
 * @author Alena
 *
 */
public class PlayingDeck extends AbstractFilteredCardStore<MagicCardGame> {
	ICardStore<IMagicCard> original;
	private int turn;

	public class ZonedFilter extends MagicCardFilter {
		HashSet<Zone> hideZones = new HashSet<MagicCardGame.Zone>();

		public ZonedFilter() {
			setNoSort();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (!(obj instanceof ZonedFilter))
				return false;
			ZonedFilter other = (ZonedFilter) obj;
			if (this.hideZones.equals(other.hideZones))
				return true;
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object clone() {
			ZonedFilter other = (ZonedFilter) super.clone();
			other.hideZones = ((HashSet<Zone>) this.hideZones.clone());
			return other;
		}

		@Override
		public boolean isFiltered(Object o) {
			boolean f = super.isFiltered(o);
			if (f)
				return f;
			if (o instanceof MagicCardGame) {
				MagicCardGame mg = (MagicCardGame) o;
				if (hideZones.contains(mg.getZone()))
					return true;
			}
			return false;
		}

		void show(Zone zone, boolean show) {
			if (show)
				hideZones.remove(zone);
			else
				hideZones.add(zone);
		}

		@Override
		public void setNoSort() {
			super.setNoSort();
			setSortField(MagicCardGameField.DRAWID, true);
		}
	}

	static class SingletonDeck<T extends ICard> extends MemoryCardStore<T> {
		@Override
		public int getCount() {
			return size();
		}
	}

	public ZonedFilter getZonedFilter() {
		return (ZonedFilter) super.getFilter();
	}

	public PlayingDeck(ICardStore<IMagicCard> store) {
		super(new SingletonDeck<MagicCardGame>());
		this.filter = new ZonedFilter();
		getZonedFilter().show(Zone.LIBRARY, false);
		getZonedFilter().show(Zone.GRAVEYARD, false);
		getZonedFilter().show(Zone.EXILE, false);
		setStore(store);
	}

	public void setStore(ICardStore<IMagicCard> store) {
		if (this.original != store) {
			this.original = store;
			newGame();
		}
	}

	public void draw(int cards) {
		int i = 0;
		for (Iterator<MagicCardGame> iterator = this.store.iterator(); iterator.hasNext() && i < cards;) {
			MagicCardGame card = iterator.next();
			if (card.getZone() == Zone.LIBRARY) {
				card.setZone(MagicCardGame.Zone.HAND);
				i++;
			}
		}
		if (i != cards)
			throw new MagicException("No more cards in the library");
	}

	public void scry(int cards) {
		int i = 0;
		for (Iterator<MagicCardGame> iterator = this.store.iterator(); iterator.hasNext() && i < cards;) {
			MagicCardGame card = iterator.next();
			if (card.getZone() == Zone.LIBRARY) {
				card.setZone(MagicCardGame.Zone.SCRY);
				i++;
			}
		}
	}

	public void newGame() {
		restart();
		draw(Math.min(7, store.size()));
	}

	/**
	 *
	 */
	public void restart() {
		((SingletonDeck<MagicCardGame>) store).clear();
		if (original != null) {
			Collection<MagicCardGame> randomize = randomize(pullIn(original));
			addAndNumber(randomize);
		}
		turn = 1;
	}

	private void addAndNumber(Collection<MagicCardGame> list) {
		int count = store.size();
		IStorage<MagicCardGame> storage = store.getStorage();
		boolean before = storage.isAutoCommit();
		storage.setAutoCommit(false);
		try {
			for (MagicCardGame mg : list) {
				mg.setDrawId(count);
				count++;
				store.add(mg);
			}
		} finally {
			storage.setAutoCommit(before);
		}
	}

	public void shuffleLibrary() {
		ArrayList<MagicCardGame> mg = new ArrayList<MagicCardGame>();
		for (Iterator<MagicCardGame> iterator = store.iterator(); iterator.hasNext();) {
			MagicCardGame card = iterator.next();
			if (card.getZone() == Zone.LIBRARY) {
				mg.add(card);
				store.remove(card);
				// iterator.remove();
			}
		}
		Collection<MagicCardGame> randomize = randomize(mg);
		addAndNumber(randomize);
	}

	public static Collection<MagicCardGame> randomize(List<MagicCardGame> list) {
		ArrayList<MagicCardGame> newList = new ArrayList<MagicCardGame>(list.size());
		Random r = new Random(System.currentTimeMillis() * list.hashCode());
		while (list.size() > 0) {
			int index = r.nextInt(list.size());
			newList.add(list.get(index));
			list.remove(index);
		}
		return newList;
	}

	private static ArrayList<MagicCardGame> pullIn(ICardStore<IMagicCard> store) {
		ArrayList<MagicCardGame> list = new ArrayList<MagicCardGame>();
		for (Iterator<IMagicCard> iterator = store.iterator(); iterator.hasNext();) {
			IMagicCard elem = iterator.next();
			int count = 1;
			if (elem instanceof ICardCountable) {
				ICardCountable card = (ICardCountable) elem;
				count = card.getCount();
				for (int i = 0; i < count; i++) {
					MagicCardGame nc = new MagicCardGame(elem);
					list.add(nc);
				}
			} else {
				list.add(new MagicCardGame(elem));
			}
		}
		return list;
	}

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getLocation() {
		return original.getLocation();
	}

	@Override
	public void clear() {
		this.original = null;
	}

	public void toZone(List<? extends IMagicCard> cardSelection, Zone zone) {
		for (Iterator<? extends IMagicCard> iterator = cardSelection.iterator(); iterator.hasNext();) {
			MagicCardGame card = (MagicCardGame) iterator.next();
			toZone(card, zone);
		}
	}

	public void toZone(MagicCardGame mg, Zone zone) {
		mg.setZone(zone);
	}

	public void showZone(Zone zone, boolean show) {
		getZonedFilter().show(zone, show);
	}

	public boolean canZone(List<IMagicCard> cardSelection, Zone zone) {
		if (cardSelection.size() == 0)
			return false;
		for (Iterator<IMagicCard> iterator = cardSelection.iterator(); iterator.hasNext();) {
			MagicCardGame card = (MagicCardGame) iterator.next();
			if (card.getZone() == zone)
				return false;
		}
		return true;
	}

	public void pushback(List<? extends IMagicCard> cardSelection) {
		ArrayList<MagicCardGame> mg = new ArrayList<MagicCardGame>();
		for (Iterator<? extends IMagicCard> iterator = cardSelection.iterator(); iterator.hasNext();) {
			MagicCardGame card = (MagicCardGame) iterator.next();
			store.remove(card);
			mg.add(card);
		}
		addAndNumber(mg);
	}

	public void newTurn() {
		turn++;
		untap();
		draw(1);
	}

	public int getTurn() {
		return turn;
	}

	public void tap(List<IMagicCard> cardSelection) {
		for (Iterator<IMagicCard> iterator = cardSelection.iterator(); iterator.hasNext();) {
			MagicCardGame card = (MagicCardGame) iterator.next();
			if (card.getZone() == Zone.BATTLEFIELD) {
				card.setTapped(!card.isTapped());
			}
		}
	}

	public void untap() {
		for (Iterator<MagicCardGame> iterator = store.iterator(); iterator.hasNext();) {
			MagicCardGame card = iterator.next();
			if (card.getZone() == Zone.BATTLEFIELD) {
				card.setTapped(false);
			}
		}
	}

	public int countInZone(Zone zone) {
		int count = 0;
		for (Iterator<MagicCardGame> iterator = store.iterator(); iterator.hasNext();) {
			MagicCardGame card = iterator.next();
			if (card.getZone() == zone) {
				count++;
			}
		}
		return count;
	}

	public int countDrawn() {
		int count = 0;
		for (Iterator<MagicCardGame> iterator = store.iterator(); iterator.hasNext();) {
			MagicCardGame card = iterator.next();
			if (card.getZone() == Zone.LIBRARY || card.getZone() == Zone.SIDEBOARD) {
				continue;
			}
			count++;
		}
		return count;
	}

	public List<MagicCardGame> getList() {
		return ((SingletonDeck<MagicCardGame>) store).getList();
	}

	public List<MagicCardGame> getListInZone(Zone zone) {
		List<MagicCardGame> res = new ArrayList<>();
		for (Iterator<MagicCardGame> iterator = store.iterator(); iterator.hasNext();) {
			MagicCardGame card = iterator.next();
			if (card.getZone() == zone) {
				res.add(card);
			}
		}
		return res;
	}
}
