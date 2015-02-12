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
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/*-
 * Format example
 * Card Name,Online,For Trade,Physical#,Rarity,Set,No.
 * Words of Wind,1,1,0,R,ONS,122/350 Standardize,1,1,0,R,ONS,116/350
 * Elvish Vanguard,1,1,0,R,ONS,259/350
 * Gigapede,1,1,0,R,ONS,264/350 Ravenous Baloth,1,1,0,R,ONS,278/350
 * Biorhythm,1,1,0,R,ONS,247/350
 * Goblin Piledriver,1,1,0,R,ONS,205/350 Tephraderm,1,1,0,R,ONS,239/350
 * Gratuitous Violence,1,1,0,R,ONS,212/350 Risky Move,1,1,0,R,ONS,223/350 Aven Brigadier,1,1,0,R,ONS,7/350
 * Aven Brigadier (premium),1,1,0,R,ONS,7/350
 *
 * Another format
 * Card Name, Online, For, Trade, Rarity, Set, No., Premium
 * Event Ticket, 2, 0, EVENT, No
 * Arrogant Bloodlord, 9, 5, U, ROE, 94/248, No
 * Arrogant Bloodlord, 1, 0, U, ROE, 94/248, Yes
 * Bala Ged Scorpion, 7, 3, C, ROE, 95/248, No
 * Bloodrite Invoker, 3, 0, C, ROE, 97/248, No
 * Bloodthrone Vampire, 13, 6, C, ROE, 98/248, No
 */
public class MtgoImportDelegate extends CsvImportDelegate {
	private int cardNameIndex = 0;
	private int countIndex = 1;
	private int forTradeIndex = -1;
	private int setIndex = 5;
	private int premiumIndex = -1;
	private int numIndex = -1;

	public MtgoImportDelegate() {
	}

	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		setUpFields();
		super.doRun(monitor);
	}

	@Override
	protected synchronized MagicCardPhysical createCard(List<String> list) {
		MagicCardPhysical x = super.createCard(list);
		try {
			if (list.get(cardNameIndex).endsWith(" (premium)")
					|| (premiumIndex >= 0 && list.get(premiumIndex).equalsIgnoreCase("Yes"))) {
				x.setSpecialTag("premium");
			}
		} catch (Exception e) {
			MagicLogger.log(e);
		}
		return x;
	}

	@Override
	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (i == cardNameIndex && value.endsWith(" (premium)")) {
			value = value.replaceAll("\\Q (premium)", "");
		}
		super.setFieldValue(card, field, i, value);
	}

	@Override
	protected void setHeaderFields(List<String> list) {
		int i = 0;
		for (Iterator iterator = list.iterator(); iterator.hasNext(); i++) {
			String name = (String) iterator.next();
			if (name.equals("Card Name")) {
				cardNameIndex = i;
			} else if (name.equals("Online") || name.equals("Quantity")) {
				countIndex = i;
			} else if (name.equals("For Trade")) {
				forTradeIndex = i;
			} else if (name.equals("Set")) {
				setIndex = i;
			} else if (name.equals("Premium")) {
				premiumIndex = i;
			} else if (name.equals("No.") || name.equals("Collector #")) {
				numIndex = i;
			}
		}
		setUpFields();
	}

	protected void setUpFields() {
		ICardField fields[] = new ICardField[10];
		fields[cardNameIndex] = MagicCardField.NAME;
		fields[countIndex] = MagicCardField.COUNT;
		if (setIndex != -1) fields[setIndex] = MagicCardField.EDITION_ABBR;
		if (forTradeIndex != -1) fields[forTradeIndex] = MagicCardField.FORTRADECOUNT;
		if (numIndex != -1) fields[numIndex] = MagicCardField.COLLNUM;
		setFields(fields);
	}
}
