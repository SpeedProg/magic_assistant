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
package com.reflexit.magiccards.core.model.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;

/**
 * @author Alena
 *
 */
public class CardStoreUtils {
	public static CardStoreUtils getInstance() {
		if (instance == null)
			instance = new CardStoreUtils();
		return instance;
	}
	public static CardStoreUtils instance;

	/**
	 * mana curve is array 0 .. 8 of card counts,
	 * where non-land is counted, 
	 * arr[8] - is cards with X cost in it, 
	 * arr[7] - is 7+
	 * @param store
	 * @return mana curve for given store
	 */
	public int[] buildManaCurve(ICardStore store) {
		int bars[] = new int[9];
		for (Iterator iterator = store.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			int cost = elem.getCmc();
			if (elem.getCost().length() == 0)
				continue; // land
			int count;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			} else {
				count = 1;
			}
			if (elem.getCost().contains("X"))
				bars[8] += count;
			else if (cost < 7 && cost >= 0)
				bars[cost] += count;
			else if (cost >= 7)
				bars[7] += count;
			else
				System.err.println("mana curve: cost:" + elem.getCost());
		}
		return bars;
	}

	public static Collection<IMagicCard> randomize(ICardStore store) {
		ArrayList<IMagicCard> filteredList = new ArrayList<IMagicCard>();
		for (Iterator<IMagicCard> iterator = store.iterator(); iterator.hasNext();) {
			IMagicCard elem = iterator.next();
			int count = 1;
			if (elem instanceof ICardCountable) {
				ICardCountable card = (ICardCountable) elem;
				count = card.getCount();
				for (int i = 0; i < count; i++) {
					MagicCardPhisical nc = new MagicCardPhisical(elem);
					nc.setCount(1);
					filteredList.add(nc);
				}
			} else {
				filteredList.add(elem);
			}
		}
		ArrayList<IMagicCard> another = new ArrayList<IMagicCard>();
		another = filteredList;
		filteredList = new ArrayList<IMagicCard>(another.size());
		Random r = new Random(System.currentTimeMillis() * another.hashCode());
		while (another.size() > 0) {
			int index = r.nextInt(another.size());
			filteredList.add(another.get(index));
			another.remove(index);
		}
		return filteredList;
	}

	private CardStoreUtils() {
	}

	public int[] buildTypeStats(ICardStore store) {
		int bars[] = new int[3]; // land, creatures, non-creatures
		for (Iterator iterator = store.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			String type = elem.getType();
			int count = 1;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			}
			if (type.contains("Land")) {
				bars[0] += count;
			} else if (type.contains("Creature") || type.contains("Summon")) {
				bars[1] += count;
			} else {
				bars[2] += count;
			}
		}
		return bars;
	}

	public CardGroup buildTypeGroups(Iterable iterable) {
		CardGroup spellNode = new CardGroup(MagicCardField.TYPE, "Spell");
		CardGroup landNode = new CardGroup(MagicCardField.TYPE, "Land");
		CardGroup basic = new CardGroup(MagicCardField.TYPE, "Basic");
		landNode.add(basic);
		CardGroup noncreatureNode = new CardGroup(MagicCardField.TYPE, "Non-Creature");
		spellNode.add(noncreatureNode);
		CardGroup creatureNode = new CardGroup(MagicCardField.TYPE, "Creature");
		spellNode.add(creatureNode);
		CardGroup instant = new CardGroup(MagicCardField.TYPE, "Instant");
		noncreatureNode.add(instant);
		CardGroup sorcery = new CardGroup(MagicCardField.TYPE, "Sorcery");
		noncreatureNode.add(sorcery);
		CardGroup ench = new CardGroup(MagicCardField.TYPE, "Enchantment");
		noncreatureNode.add(ench);
		CardGroup artifact = new CardGroup(MagicCardField.TYPE, "Artifact");
		noncreatureNode.add(artifact);
		CardGroup walker = new CardGroup(MagicCardField.TYPE, "Planeswalker");
		noncreatureNode.add(walker);
		int total = 0;
		for (Iterator iterator = iterable.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			String type = elem.getType();
			int count = 1;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			}
			if (type.contains("Land")) {
				if (type.contains(basic.getName())) {
					basic.add(elem);
					landNode.addCount(count);
				} else {
					landNode.add(elem);
				}
			} else {
				spellNode.addCount(count);
				if (type.contains(creatureNode.getName()) || type.contains("Summon")) {
					creatureNode.add(elem);
				} else {
					noncreatureNode.addCount(count);
					if (type.contains(instant.getName()) || type.contains("Interrupt")) {
						instant.add(elem);
					} else if (type.contains(ench.getName())) {
						ench.add(elem);
					} else if (type.contains(sorcery.getName())) {
						sorcery.add(elem);
					} else if (type.contains(artifact.getName())) {
						artifact.add(elem);
					} else if (type.contains(walker.getName())) {
						walker.add(elem);
					} else {
						noncreatureNode.addCount(-count);
						noncreatureNode.add(elem);
					}
				}
			}
			total += count;
		}
		CardGroup root = new CardGroup(MagicCardField.TYPE, "");
		root.setCount(total);
		root.add(landNode);
		root.add(spellNode);
		return root;
	}
}
