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
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/*-
 Name,Edition,Qty,Foil,Market Price,Market Value,Added,Color,Type,Rarity,Cost,P/T,Text
 Abandoned Outpost,OD,2,False,0.11,0.22,13/12/2009 2:36:33 PM,L,Land,C,,,"Abandoned Outpost comes into play tapped. {T}: Add {W} to your mana pool. {T}, Sacrifice Abandoned Outpost: Add one mana of any color to your mana pool."
 Abattoir Ghoul,ISD,2,False,0.20,0.40,13/10/2011 5:13:06 PM,B,Creature — Zombie,U,3B,3/2,"First strike Whenever a creature dealt damage by Abattoir Ghoul this turn dies, you gain life equal to that creature's toughness."
 Abbey Griffin,ISD,4,False,0.11,0.44,13/10/2011 5:15:18 PM,W,Creature — Griffin,C,3W,2/2,"Flying, vigilance
 */
public class MTGStudioCsvImportDelegate extends CsvImportDelegate {
	private int cardNameIndex = 0;
	private int countIndex = 2;
	private int setIndex = 1;
	private int foilIndex = 3;
	private int addedIndex;
	private int fields = 13;

	@Override
	public ReportType getType() {
		return ReportType.createReportType("csv", "MTG Studio CSV");
	}

	public MTGStudioCsvImportDelegate() {
	}

	@Override
	public char getSeparator() {
		return ',';
	}

	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		setUpFields();
		super.doRun(monitor);
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
			}
			String nameByAbbr = Editions.getInstance().getNameByAbbr(value);
			if (nameByAbbr == null)
				nameByAbbr = value;
			card.setObjectByField(MagicCardField.SET, nameByAbbr);
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
			card.setObjectByField(field, name);
			return;
		}
		card.setObjectByField(field, value);
	}

	@Override
	protected void importCard(MagicCardPhysical card) {
		super.importCard(card);
		Object err = card.getError();
		if (err != null && err.toString().startsWith("Name not found")) {
			ICardStore lookupStore = DataManager.getCardHandler().getMagicDBStore();
			if (lookupStore != null) {
				String name = card.getName();
				String tryName = name;
				if (tryName.contains("/")) {
					tryName = tryName.substring(0, tryName.indexOf('/'));
				}
				if (tryToFindName(card, lookupStore, tryName)) {
					// System.err.println("^^^ resolved " + card + ": " + card.getError());
				} else {
					MagicLogger.log("NOT resolved " + card + ": " + err);
				}
			}
		}
	}

	public boolean tryToFindName(MagicCardPhysical card, ICardStore lookupStore, String tryName) {
		String set = card.getSet();
		List<IMagicCard> candidates = ImportUtils.getCandidates(tryName, set, lookupStore, true);
		if (candidates.size() > 0) {
			IMagicCard base = candidates.get(0);
			card.setError(null);
			card.setObjectByField(MagicCardField.NAME, base.getName());
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
		setUpFields();
	}

	protected void setUpFields() {
		ICardField fields[] = new ICardField[this.fields];
		fields[cardNameIndex] = MagicCardField.NAME;
		if (countIndex >= 0)
			fields[countIndex] = MagicCardFieldPhysical.COUNT;
		if (setIndex >= 0)
			fields[setIndex] = MagicCardField.SET;
		setFields(fields);
	}
}
