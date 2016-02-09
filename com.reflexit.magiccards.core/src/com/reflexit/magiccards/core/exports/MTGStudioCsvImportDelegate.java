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
package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.exports.ImportError.Type;
import com.reflexit.magiccards.core.exports.ImportUtils.LookupHash;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/*-
 Name,Edition,Qty,Foil,Market Price,Market Value,Added,Color,Type,Rarity,Cost,P/T,Text
 Abandoned Outpost,OD,2,False,0.11,0.22,13/12/2009 2:36:33 PM,L,Land,C,,,"Abandoned Outpost comes into play tapped. {T}: Add {W} to your mana pool. {T}, Sacrifice Abandoned Outpost: Add one mana of any color to your mana pool."
 Abattoir Ghoul,ISD,2,False,0.20,0.40,13/10/2011 5:13:06 PM,B,Creature — Zombie,U,3B,3/2,"First strike Whenever a creature dealt damage by Abattoir Ghoul this turn dies, you gain life equal to that creature's toughness."
 Abbey Griffin,ISD,4,False,0.11,0.44,13/10/2011 5:15:18 PM,W,Creature — Griffin,C,3W,2/2,"Flying, vigilance"
 */
public class MTGStudioCsvImportDelegate extends CsvImportDelegate {
	private int cardNameIndex = 0;
	private int countIndex = 2;
	private int setIndex = 1;
	private int foilIndex = 3;
	private int addedIndex;
	private int fields = 13;
	private LookupHash lookup;

	public MTGStudioCsvImportDelegate() {
	}

	@Override
	public String getExample() {
		return ""
				+ "Name,Edition,Qty,Foil,Market Price,Market Value,Added,Color,Type,Rarity,Cost,P/T,Text\n"
				+ "Abandoned Outpost,OD,2,False,0.11,0.22,13/12/2009 2:36:33 PM,L,Land,C,,,\"Abandoned Outpost comes into play tapped. {T}: Add {W} to your mana pool. {T}, Sacrifice Abandoned Outpost: Add one mana of any color to your mana pool.\"\n"
				+ "Abattoir Ghoul,ISD,2,False,0.20,0.40,13/10/2011 5:13:06 PM,B,Creature — Zombie,U,3B,3/2,\"First strike Whenever a creature dealt damage by Abattoir Ghoul this turn dies, you gain life equal to that creature's toughness.\"\n"
				+ "Abbey Griffin,ISD,4,False,0.11,0.44,13/10/2011 5:15:18 PM,W,Creature — Griffin,C,3W,2/2,\"Flying, vigilance\"\n";
	}

	@Override
	public char getSeparator() {
		return ',';
	}

	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		lookup = new ImportUtils.LookupHash(DataManager.getInstance().getMagicDBStore());
		setUpFields();
		super.doRun(monitor);
		lookup = null;
	}

	@Override
	protected synchronized MagicCardPhysical createCard(List<String> list) {
		try {
			if (list.get(cardNameIndex).length() == 0)
				return null;
			if (list.get(setIndex).length() == 0)
				return null;
		} catch (Exception e) {
			MagicLogger.log(e);
		}
		// set standard fields
		MagicCardPhysical x = super.createCard(list);
		// special fields
		try {
			String comment = "";
			if (foilIndex >= 0 && list.get(foilIndex).equals("true"))
				comment += "foil,";
			if (addedIndex >= 0)
				comment += list.get(addedIndex);
			if (comment.length() > 0)
				x.setComment(comment);
		} catch (Exception e) {
			MagicLogger.log(e);
		}
		return x;
	}

	@Override
	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field == MagicCardField.SET) {
			if (value.equals("LE")) {
				value = "LGN"; // special case LE uses for diffrent set in gatherer
			} else if (value.equals("PR")) {
				value = "PPR"; // PR is promo not Prophecy
			} else if (value.equals("PY")) {
				value = "PR"; // PY is Prophecy
			}
			String nameByAbbr = Editions.getInstance().getNameByAbbr(value);
			if (nameByAbbr == null)
				nameByAbbr = value;
			card.set(MagicCardField.SET, nameByAbbr);
			return;
		} else if (field == MagicCardField.NAME) {
			String name = value;
			if (name.endsWith(")")) {
				name = name.replaceAll(" \\(\\d+\\)$", "");
			}
			if (name.endsWith("]")) {
				name = name.replaceAll(" \\[.*\\]$", "");
			}
			if (name.contains("Aether")) {
				name = name.replaceAll("Ae", "Æ");
			}
			card.set(field, name);
			return;
		}
		card.set(field, value);
	}

	@Override
	protected void importCard(MagicCardPhysical card) {
		Edition ed = ImportUtils.resolveSet(card.getSet());
		String abbr = null;
		if (ed != null) {
			card.getBase().setSet(ed.getName());
			abbr = ed.getMainAbbreviation();
		}
		List<IMagicCard> candidates = lookup.getCandidates(card.getName(), card.getSet());
		if (candidates.size() > 0) {
			IMagicCard base = candidates.get(0);
			card.setMagicCard((MagicCard) base);
			importData.add(card);
		} else {
			candidates = lookup.getCandidates(card.getName());
			boolean found = false;
			if (candidates.size() > 0) {
				for (Iterator iterator = candidates.iterator(); iterator.hasNext();) {
					IMagicCard base = (IMagicCard) iterator.next();
					Edition ed2 = base.getEdition();
					String abbr2 = ed2.getMainAbbreviation();
					// String extra = ed2.getExtraAbbreviations();
					// System.err.println("Looking for " + card.getName() + " from " + abbr +
					// " found in " + abbr2 + " " + ed2 + " " + extra);
					if ("TSP".equals(abbr) && "TSB".equals(abbr2) || "MED".equals(abbr)
							&& "ME2".equals(abbr2)) {
						card.setMagicCard((MagicCard) base);
						importData.add(card);
						found = true;
						break;
					}
				}
			}
			if (!found)
				super.importCard(card);
			if (!found)
				ImportUtils.updateCardReference(card); // XXX
			// System.err.println(card);
		}
		Object err = card.getError();
		if (err instanceof ImportError
				&& (((ImportError) err).getType().equals(Type.NAME_NOT_FOUND_IN_DB) || ((ImportError) err)
						.getType().equals(
								Type.NAME_NOT_FOUND_IN_SET))) {
			String name = card.getName();
			if (tryToFindName(card, name)) {
				return;
			}
			if (name.contains("/")) {
				String tryName = name.substring(0, name.indexOf('/'));
				if (tryToFindName(card, tryName)) {
					return;
				}
				tryName = name.replaceAll("/", " // ");
				if (tryToFindName(card, tryName)) {
					return;
				}
			}
			// MagicLogger.log("NOT resolved " + card + ": " + err);
		}
	}

	public boolean tryToFindName(MagicCardPhysical card, String tryName) {
		String set = card.getSet();
		List<IMagicCard> candidates = lookup.getCandidates(tryName, set);
		if (candidates.size() > 0) {
			IMagicCard base = candidates.get(0);
			card.setError(null);
			card.set(MagicCardField.NAME, base.getName());
			// card.setObjectByField(MagicCardField.ID, base.getCardId());
			ImportUtils.updateCardReference(card);
			if (card.getError() == null)
				return true;
		}
		return false;
	}

	@Override
	protected void setHeaderFields(List<String> list) {
		cardNameIndex = setIndex = countIndex = foilIndex = addedIndex = -1;
		fields = list.size();
		int i = 0;
		for (Iterator iterator = list.iterator(); iterator.hasNext(); i++) {
			String name = (String) iterator.next();
			if (name.equals("Name")) {
				cardNameIndex = i;
			} else if (name.equals("Edition")) {
				setIndex = i;
			} else if (name.equals("Qty")) {
				countIndex = i;
			} else if (name.equals("Foil")) {
				foilIndex = i;
			} else if (name.equals("Added")) {
				addedIndex = i;
			}
		}
		if (cardNameIndex < 0)
			throw new IllegalArgumentException("Name field is not found. Wrong format?");
		setUpFields();
	}

	protected void setUpFields() {
		ICardField fields[] = new ICardField[this.fields];
		fields[cardNameIndex] = MagicCardField.NAME;
		if (countIndex >= 0)
			fields[countIndex] = MagicCardField.COUNT;
		if (setIndex >= 0)
			fields[setIndex] = MagicCardField.SET;
		setFields(fields);
	}
}
