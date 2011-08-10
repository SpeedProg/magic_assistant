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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import com.reflexit.magiccards.core.CoreMessages;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.Colors;
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
	 * mana curve is array 0 .. 8 of card counts, where non-land is counted,
	 * arr[8] - is cards with X cost in it, arr[7] - is 7+
	 * 
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
			if (elem.getCost().contains("X")) //$NON-NLS-1$
				bars[8] += count;
			else if (cost < 7 && cost >= 0)
				bars[cost] += count;
			else if (cost >= 7)
				bars[7] += count;
			else
				System.err.println("mana curve: cost:" + elem.getCost()); //$NON-NLS-1$
		}
		return bars;
	}

	public float getAverageManaCost(ICardStore store) {
		int total = 0;
		int sum = 0;
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
			sum += cost;
			total += count;
		}
		if (total == 0)
			return 0;
		return sum / (float) total;
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
					MagicCardPhisical nc = new MagicCardPhisical(elem, store.getLocation());
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
			if (type == null)
				continue;
			int count = 1;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			}
			if (type.contains(CoreMessages.CardTypes_Land)) {
				bars[0] += count;
			} else if (type.contains(CoreMessages.CardTypes_Creature) || type.contains(CoreMessages.CardTypes_Summon)) {
				bars[1] += count;
			} else {
				bars[2] += count;
			}
		}
		return bars;
	}

	public static Collection<String> buildColors(Iterable store) {
		HashSet<String> colors = new HashSet<String>();
		for (Object element : store) {
			IMagicCard elem = (IMagicCard) element;
			if (elem.getType().contains(CoreMessages.CardTypes_Land))
				continue;
			String name = Colors.getColorName(elem.getCost());
			String[] split = name.split("-"); //$NON-NLS-1$
			for (String c : split) {
				colors.add(c);
			}
		}
		return colors;
	}

	public static Collection<CardGroup> buildSpellColorStats(Iterable store) {
		HashMap<CardGroup, CardGroup> groupsList = new HashMap();
		for (Object element : store) {
			IMagicCard elem = (IMagicCard) element;
			if (elem.getType() == null || elem.getType().contains(CoreMessages.CardTypes_Land))
				continue;
			String name = Colors.getColorName(elem.getCost());
			CardGroup g = new CardGroup(MagicCardField.COST, name);
			if (groupsList.containsKey(g)) {
				groupsList.get(g).addCount(1);
			} else {
				g.addCount(1);
				groupsList.put(g, g);
			}
		}
		return groupsList.keySet();
	}

	public CardGroup buildTypeGroups(Iterable iterable) {
		CardGroup spellNode = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Spell);
		CardGroup landNode = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Land);
		CardGroup unknownNode = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Unknown);
		CardGroup basic = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Basic);
		landNode.add(basic);
		CardGroup noncreatureNode = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Non_Creature);
		spellNode.add(noncreatureNode);
		CardGroup creatureNode = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Creature);
		spellNode.add(creatureNode);
		CardGroup instant = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Instant);
		noncreatureNode.add(instant);
		CardGroup sorcery = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Sorcery);
		noncreatureNode.add(sorcery);
		CardGroup ench = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Enchantment);
		noncreatureNode.add(ench);
		CardGroup artifact = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Artifact);
		noncreatureNode.add(artifact);
		CardGroup walker = new CardGroup(MagicCardField.TYPE, CoreMessages.CardTypes_Planeswalker);
		noncreatureNode.add(walker);
		int total = 0;
		for (Iterator iterator = iterable.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			int count = 1;
			try {
				String type = elem.getType();
				if (type == null) {
					unknownNode.add(elem);
					continue;
				}
				if (elem instanceof ICardCountable) {
					count = ((ICardCountable) elem).getCount();
				}
				if (type.contains(CoreMessages.CardTypes_Land)) {
					if (type.contains(basic.getName())) {
						basic.add(elem);
						landNode.addCount(count);
					} else {
						landNode.add(elem);
					}
				} else {
					spellNode.addCount(count);
					if (type.contains(creatureNode.getName()) || type.contains(CoreMessages.CardTypes_Summon)) {
						creatureNode.add(elem);
					} else {
						noncreatureNode.addCount(count);
						if (type.contains(instant.getName()) || type.contains(CoreMessages.CardTypes_Interrupt)) {
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
			} catch (Exception e) {
				unknownNode.add(elem);
			}
			total += count;
		}
		CardGroup root = new CardGroup(MagicCardField.TYPE, ""); //$NON-NLS-1$
		root.setCount(total);
		root.add(landNode);
		root.add(spellNode);
		if (unknownNode.getCount() > 0)
			root.add(unknownNode);
		return root;
	}
}
