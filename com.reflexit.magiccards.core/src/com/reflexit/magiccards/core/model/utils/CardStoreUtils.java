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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.locale.CardText;
import com.reflexit.magiccards.core.model.Abilities;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.ICardStore;

/**
 * @author Alena
 *
 */
public final class CardStoreUtils {
	private static final String OTHERS = "Others";

	public synchronized static CardStoreUtils getInstance() {
		if (instance == null)
			instance = new CardStoreUtils();
		return instance;
	}

	public static CardStoreUtils instance;
	private static CardTypes MTYPES = CardTypes.getInstance();

	private CardStoreUtils() {
		// CardTextEN.EN = "EN"; // fake initializion
	}

	public static ICardGroup buildManaCurveGroup(Iterable iterable) {
		CardGroup manaNode = new CardGroup(MagicCardField.COST, "Mana Curve");
		CardGroup zeroNode = new CardGroup(MagicCardField.COST, "0");
		CardGroup oneNode = new CardGroup(MagicCardField.COST, "1");
		CardGroup twoNode = new CardGroup(MagicCardField.COST, "2");
		CardGroup threeNode = new CardGroup(MagicCardField.COST, "3");
		CardGroup fourNode = new CardGroup(MagicCardField.COST, "4");
		CardGroup fiveNode = new CardGroup(MagicCardField.COST, "5");
		CardGroup sixNode = new CardGroup(MagicCardField.COST, "6");
		CardGroup sevenOrMoreNode = new CardGroup(MagicCardField.COST, "7+");
		CardGroup[] cardGroups = { zeroNode, oneNode, twoNode, threeNode, fourNode, fiveNode, sixNode,
				sevenOrMoreNode };
		for (Iterator iterator = iterable.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			int cost = elem.getCmc();
			if (elem.getCost() == null || elem.getCost().length() == 0)
				continue; // land
			else if (cost < 7 && cost >= 0)
				cardGroups[cost].add(elem);
			else if (cost >= 7)
				cardGroups[7].add(elem);
		}
		ICardGroup root = new CardGroup(MagicCardField.COST, ""); //$NON-NLS-1$
		for (CardGroup cardGroup : cardGroups) {
			manaNode.add(cardGroup);
		}
		root.add(manaNode);
		return root;
	}

	public static String buildColors(Iterable store) {
		String res = "";
		if (store == null)
			return res;
		HashSet<String> colors = new HashSet<String>();
		Colors cinstance = Colors.getInstance();
		for (Object element : store) {
			IMagicCard elem = (IMagicCard) element;
			String cost = elem.getCost();
			if (cost == null || cost.isEmpty())
				continue;
			Collection<String> colorIdentify = cinstance.getColorPresense(cost);
			if (colorIdentify.isEmpty())
				colors.add(Colors.ManaColor.COLORLESS.tag());
			else
				colors.addAll(colorIdentify);
		}
		for (String tag : cinstance.sortTags(colors)) {
			res += "{" + tag + "}";
		}
		return res;
	}

	public static ICardGroup buildCreatureGroups(Iterable iterable) {
		CardGroup unknownNode = new CardGroup(MagicCardField.TYPE, CardText.Type_Unknown);
		CardGroup creatureNode = new CardGroup(MagicCardField.TYPE, CardText.Type_Creature);
		for (Iterator iterator = iterable.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			try {
				String type = elem.getType();
				if (type == null) {
					unknownNode.add(elem);
					continue;
				}
				if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Creature)) {
					String subAnsSuperTypes[] = type.split("[:-]+", 2); // : in
																		// french
					String creatureSuperType = subAnsSuperTypes[0].trim();
					String creatureSubType = subAnsSuperTypes.length > 1 ? subAnsSuperTypes[1].trim() : "";
					if (!creatureSuperType.isEmpty()) {
						CardGroup superCardGroup = creatureNode.getSubGroup(creatureSuperType);
						if (superCardGroup == null) {
							superCardGroup = new CardGroup(MagicCardField.TYPE, creatureSuperType);
							creatureNode.add(superCardGroup);
						}
						if (!creatureSubType.isEmpty()) {
							String subTypes[] = creatureSubType.split("[ ,]+");
							for (int i = 0; i < subTypes.length; i++) {
								String key = subTypes[i];
								CardGroup subTypeCardGroup = superCardGroup.getSubGroup(key);
								if (subTypeCardGroup == null) {
									subTypeCardGroup = new CardGroup(MagicCardField.TYPE, key);
									superCardGroup.add(subTypeCardGroup);
								}
								subTypeCardGroup.add(elem);
							}
						} else {
							superCardGroup.add(elem);
						}
					} else {
						creatureNode.add(elem);
					}
				}
			} catch (Exception e) {
				unknownNode.add(elem);
			}
		}
		ICardGroup root = new CardGroup(MagicCardField.TYPE, ""); //$NON-NLS-1$
		root.add(creatureNode);
		if (unknownNode.getCount() > 0)
			root.add(unknownNode);
		return root;
	}

	public static class Pair {
		public String key;
		public int value;

		public Pair(String key, int value) {
			super();
			this.key = key;
			this.value = value;
		}
	}

	public static Map<String, Integer> top(int n, HashMap<String, Integer> stats) {
		List<Pair> list = new ArrayList<Pair>();
		for (Iterator<String> iterator = stats.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if (!key.equals(OTHERS)) {
				list.add(new Pair(key, stats.get(key)));
			}
		}
		Collections.sort(list, new Comparator<Pair>() {
			@Override
			public int compare(Pair o1, Pair o2) {
				return o2.value - o1.value;
			}
		});
		LinkedHashMap<String, Integer> res = new LinkedHashMap<String, Integer>();
		int i = 0;
		Integer iothers = stats.get(OTHERS);
		int others = iothers == null ? 0 : iothers;
		for (Iterator iterator = list.iterator(); iterator.hasNext(); i++) {
			Pair pair = (Pair) iterator.next();
			if (i < n)
				res.put(pair.key, pair.value);
			else
				others += pair.value;
		}
		if (others > 0)
			res.put(OTHERS, others);
		return res;
	}

	public static HashMap<String, Integer> buildCreatureStats(ICardStore store) {
		HashMap<String, Integer> subCreaturesCount = new HashMap<String, Integer>();
		for (Iterator iterator = store.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			int count = 1;
			try {
				String type = elem.getType();
				if (type == null) {
					continue;
				}
				if (elem instanceof ICardCountable) {
					count = ((ICardCountable) elem).getCount();
				}
				if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Creature)) {
					String subAnsSuperTypes[] = type.split("[:-]+", 2); // : in
																		// french
					// String creatureSuperType = subAnsSuperTypes[0].trim();
					String creatureSubType = subAnsSuperTypes.length > 1 ? subAnsSuperTypes[1].trim() : "";
					if (!creatureSubType.isEmpty()) {
						String subTypes[] = creatureSubType.split("[ ,]+");
						for (int i = 0; i < subTypes.length; i++) {
							String key = subTypes[i];
							inc(subCreaturesCount, key, count);
						}
					} else {
						inc(subCreaturesCount, OTHERS, count);
					}
				}
			} catch (Exception e) {
				MagicLogger.log(e);
			}
		}
		return subCreaturesCount;
	}

	private static void inc(HashMap<String, Integer> subCreaturesCount, String key, int count) {
		if (!subCreaturesCount.containsKey(key)) {
			subCreaturesCount.put(key, count);
		} else {
			subCreaturesCount.put(key, subCreaturesCount.get(key) + count);
		}
	}

	/**
	 * mana curve is array 0 .. 7 of card counts, where non-land is counted,
	 * arr[7] - is 7+
	 *
	 * @param store
	 * @return mana curve for given store
	 */
	public static int[] buildManaCurveStats(ICardStore store) {
		int bars[] = new int[8];
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
			if (cost < 7 && cost >= 0)
				bars[cost] += count;
			else if (cost >= 7)
				bars[7] += count;
		}
		return bars;
	}

	public static ICardGroup buildSpellColorGroups(Iterable store) {
		CardGroup colorNode = new CardGroup(MagicCardField.COST, "Colour");
		for (Object element : store) {
			IMagicCard elem = (IMagicCard) element;
			if (isLand(elem))
				continue;
			String scost = elem.getCost();
			String name = Colors.getColorName(scost);
			colorNode.addToSubGroup(name, elem);
		}
		ICardGroup root = new CardGroup(MagicCardField.COST, ""); //$NON-NLS-1$
		root.add(colorNode);
		return root;
	}

	public static boolean isLand(IMagicCard elem) {
		return elem.getType() == null || MTYPES.hasType(elem, CardTypes.TYPES.Type_Land);
	}

	public static HashMap<String, Integer> countStats(ICardGroup group) {
		HashMap<String, Integer> groupsList = new HashMap<String, Integer>();
		for (ICard elem : group) {
			int count = 1;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			}
			groupsList.put(elem.getName(), count);
		}
		return groupsList;
	}

	public static HashMap<String, Integer> buildSpellColorStats(Iterable store) {
		return countStats((ICardGroup) buildSpellColorGroups(store).getChildAtIndex(0));
	}

	public static CardGroup buildTypeGroups(Iterable iterable) {
		CardGroup typeNode = new CardGroup(MagicCardField.TYPE, "Type");
		CardGroup landNode = new CardGroup(MagicCardField.TYPE, CardText.Type_Land);
		typeNode.add(landNode);
		CardGroup unknownNode = new CardGroup(MagicCardField.TYPE, CardText.Type_Unknown);
		CardGroup basic = new CardGroup(MagicCardField.TYPE, CardText.Type_Basic);
		landNode.add(basic);
		CardGroup nonbasic = new CardGroup(MagicCardField.TYPE, CardText.Type_Non_Basic);
		landNode.add(nonbasic);
		CardGroup noncreatureNode = new CardGroup(MagicCardField.TYPE, CardText.Type_Non_Creature);
		typeNode.add(noncreatureNode);
		CardGroup creatureNode = new CardGroup(MagicCardField.TYPE, CardText.Type_Creature);
		typeNode.add(creatureNode);
		CardGroup instant = new CardGroup(MagicCardField.TYPE, CardText.Type_Instant);
		noncreatureNode.add(instant);
		CardGroup sorcery = new CardGroup(MagicCardField.TYPE, CardText.Type_Sorcery);
		noncreatureNode.add(sorcery);
		CardGroup ench = new CardGroup(MagicCardField.TYPE, CardText.Type_Enchantment);
		noncreatureNode.add(ench);
		CardGroup artifact = new CardGroup(MagicCardField.TYPE, CardText.Type_Artifact);
		noncreatureNode.add(artifact);
		CardGroup walker = new CardGroup(MagicCardField.TYPE, CardText.Type_Planeswalker);
		noncreatureNode.add(walker);
		for (Iterator iterator = iterable.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			try {
				String type = elem.getType();
				if (type == null) {
					unknownNode.add(elem);
					continue;
				}
				if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Land)) {
					if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Basic)) {
						basic.add(elem);
					} else {
						nonbasic.add(elem);
					}
				} else {
					if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Creature)) {
						String trimmedType = elem.getType().trim();
						int subTypeSeparatorPos = trimmedType.indexOf("-");
						String creatureSubTypeText = "";
						String creatureSubSubTypeText = "";
						if (subTypeSeparatorPos >= 0) {
							creatureSubTypeText = trimmedType.substring(0, subTypeSeparatorPos).trim();
							creatureSubSubTypeText = trimmedType.substring(subTypeSeparatorPos + 1).trim();
							int subSubTypeSeparatorPos = creatureSubSubTypeText.indexOf(" ");
							if (subSubTypeSeparatorPos >= 0) {
								creatureSubSubTypeText = creatureSubSubTypeText.substring(0,
										subSubTypeSeparatorPos).trim();
							}
						}
						if (!creatureSubTypeText.isEmpty()) {
							CardGroup subTypeCardGroup = creatureNode.getSubGroup(creatureSubTypeText);
							if (subTypeCardGroup == null) {
								subTypeCardGroup = new CardGroup(MagicCardField.TYPE, creatureSubTypeText);
								creatureNode.add(subTypeCardGroup);
							}
							if (!creatureSubSubTypeText.isEmpty()) {
								CardGroup subSubTypeCardGroup = subTypeCardGroup
										.getSubGroup(creatureSubSubTypeText);
								if (subSubTypeCardGroup == null) {
									subSubTypeCardGroup = new CardGroup(MagicCardField.TYPE,
											creatureSubSubTypeText);
									subTypeCardGroup.add(subSubTypeCardGroup);
								}
								subSubTypeCardGroup.add(elem);
							} else {
								subTypeCardGroup.add(elem);
							}
						} else {
							creatureNode.add(elem);
						}
					} else {
						if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Instant)) {
							instant.add(elem);
						} else if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Enchantment)) {
							ench.add(elem);
						} else if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Sorcery)) {
							sorcery.add(elem);
						} else if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Artifact)) {
							artifact.add(elem);
						} else if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Planeswalker)) {
							walker.add(elem);
						} else {
							unknownNode.add(elem);
						}
					}
				}
			} catch (Exception e) {
				unknownNode.add(elem);
			}
		}
		CardGroup root = new CardGroup(MagicCardField.TYPE, ""); //$NON-NLS-1$

		root.add(typeNode);

		if (unknownNode.getCount() > 0)
			typeNode.add(unknownNode);
		return root;
	}

	public static int[] buildTypeStats(ICardStore store) {
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
			if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Land)) {
				bars[0] += count;
			} else if (MTYPES.hasType(elem, CardTypes.TYPES.Type_Creature)) {
				bars[1] += count;
			} else {
				bars[2] += count;
			}
		}
		return bars;
	}

	public static int countCards(Iterable store) {
		int count = 0;
		synchronized (store) {
			for (Object element : store) {
				if (element instanceof ICardCountable) {
					count += ((ICardCountable) element).getCount();
				} else {
					count++;
				}
			}
			return count;
		}
	}

	public static int getManaCost(Iterable store) {
		if (store == null)
			return 0;
		int sum = 0;
		for (Object card : store) {
			IMagicCard elem = (IMagicCard) card;
			int cost = elem.getCmc();
			int count;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			} else {
				count = 1;
			}
			sum += cost * count;
		}
		return sum;
	}

	public static int getCount(Iterable store) {
		if (store == null)
			return 0;
		int sum = 0;
		for (Object card : store) {
			IMagicCard elem = (IMagicCard) card;
			int count;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			} else {
				count = 1;
			}
			sum += count;
		}
		return sum;
	}

	public static ICardGroup buildAbilityGroups(Iterable iterable) {
		HashMap<String, CardGroup> abilityGroups = new HashMap<String, CardGroup>();
		HashMap<String, CardGroup> minedAbilityGroups = new HashMap<String, CardGroup>();
		Abilities.IAbilityMatcher[] abilities = Abilities.getAbilities();
		for (Iterator iterator = iterable.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			String text = elem.getOracleText();
			if (text == null || text.length() == 0)
				continue;
			for (int i = 0; i < abilities.length; i++) {
				if (abilities[i].match(text)) {
					HashMap<String, CardGroup> groups;
					if (abilities[i].isKeyword()) {
						groups = abilityGroups;
					} else {
						groups = minedAbilityGroups;
					}
					String name = abilities[i].getDisplayName();
					if (groups.containsKey(name)) {
						groups.get(name).add(elem);
					} else {
						groups.put(name, new CardGroup(MagicCardField.TEXT, name));
						groups.get(name).add(elem);
					}
				}
			}
		}
		ICardGroup root = new CardGroup(MagicCardField.TEXT, ""); //$NON-NLS-1$
		CardGroup abilityNode = new CardGroup(MagicCardField.TEXT, "Ability");
		CardGroup abilityKeywordNode = new CardGroup(MagicCardField.TEXT, "Keyword Ability");
		CardGroup minedAbilityNode = new CardGroup(MagicCardField.TEXT, "Mined Ability");
		root.add(abilityNode);
		abilityNode.add(abilityKeywordNode);
		abilityNode.add(minedAbilityNode);
		for (CardGroup cardGroup : abilityGroups.values()) {
			abilityKeywordNode.add(cardGroup);
		}
		for (CardGroup cardGroup : minedAbilityGroups.values()) {
			minedAbilityNode.add(cardGroup);
		}
		return root;
	}

	public static HashMap<String, Integer> buildAbilityStats(ICardStore store) {
		HashMap<String, Integer> abilityCount = new HashMap<String, Integer>();
		Abilities.IAbilityMatcher[] abilities = Abilities.getAbilities();
		for (Iterator iterator = store.iterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			String text = elem.getOracleText();
			if (text == null || text.length() == 0)
				continue;
			int count = 1;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			}
			for (int i = 0; i < abilities.length; i++) {
				if (abilities[i].isKeyword()) {
					if (abilities[i].match(text)) {
						String name = abilities[i].getDisplayName();
						if (abilityCount.containsKey(name)) {
							abilityCount.put(name, abilityCount.get(name) + count);
						} else {
							abilityCount.put(name, count);
						}
					}
				}
			}
		}
		return abilityCount;
	}

	public static int getMaxRepeats(Iterable store) {
		CountersMap buildRepeats = buildRepeats(store, null);
		return buildRepeats.max();
	}

	public static CountersMap buildRepeats(Iterable store, CountersMap nameToCount) {
		if (nameToCount == null)
			nameToCount = new CountersMap();
		if (store != null) {
			for (Object card : store) {
				if (card instanceof IMagicCard && card instanceof ICardCountable) {
					IMagicCard mc = (IMagicCard) card;
					if (mc.getBase().isBasicLand()) {
						continue;
					}
					nameToCount.inc(mc.getName(), ((ICardCountable) mc).getCount());
				}
			}
		}
		return nameToCount;
	}

	public static CardGroup buildGroup(ICardStore<IMagicCard> mainStore, ICardStore<IMagicCard> sideboardStore) {
		CardGroup group = new CardGroup(MagicCardField.ID, "...");
		group.addAll(mainStore);
		group.addAll(sideboardStore);
		return group;
	}

	public static class CardStats {
		public int mainCount;
		public int sideboardCount;
		public String mainColors;
		public String sideboardColors;
		public int maxRepeats;

		public CardStats(ICardStore<IMagicCard> store) {
			super();
			calculate(store);
		}

		private void calculate(ICardStore<IMagicCard> store) {
			Location location = store.getLocation();
			Location sideboard = location.toSideboard();
			ICardStore<IMagicCard> sideboardStore = DataManager.getInstance().getCardStore(sideboard);
			ICardStore<IMagicCard> mainStore = DataManager.getInstance().getCardStore(location.toMainDeck());
			if (mainStore == null)
				mainStore = store;
			sideboardCount = getCount(sideboardStore);
			mainCount = getCount(mainStore);
			mainColors = CardStoreUtils.buildColors(mainStore);
			sideboardColors = CardStoreUtils.buildColors(sideboardStore);
			maxRepeats = CardStoreUtils.getMaxRepeats(store);
		}
	}
}
