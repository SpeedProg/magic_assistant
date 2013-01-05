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

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
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
		String name = list.get(cardNameIndex);
		if (name.length() == 0)
			return null;
		MagicCardPhysical x = super.createCard(list);
		try {
			String comment = "";
			if (list.get(foilIndex).equals("true"))
				comment += "foil,";
			comment += list.get(addedIndex);
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
		} else if (field == MagicCardField.NAME && value.endsWith(")")) {
			value = value.replaceAll(" \\(\\d+\\)$", "");
			card.setObjectByField(field, value);
		} else {
			card.setObjectByField(field, value);
		}
	}

	@Override
	protected void setHeaderFields(List<String> list) {
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
		fields[countIndex] = MagicCardFieldPhysical.COUNT;
		fields[setIndex] = MagicCardField.SET;
		setFields(fields);
	}
}
