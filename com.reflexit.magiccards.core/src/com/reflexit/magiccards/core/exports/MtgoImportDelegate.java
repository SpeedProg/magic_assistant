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

import java.util.List;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * Format example
Card Name,Online,For Trade,Physical#,Rarity,Set,No. 
Words of Wind,1,1,0,R,ONS,122/350 
Standardize,1,1,0,R,ONS,116/350 
Elvish Vanguard,1,1,0,R,ONS,259/350 
Gigapede,1,1,0,R,ONS,264/350 
Ravenous Baloth,1,1,0,R,ONS,278/350 
Biorhythm,1,1,0,R,ONS,247/350 
Goblin Piledriver,1,1,0,R,ONS,205/350 
Tephraderm,1,1,0,R,ONS,239/350 
Gratuitous Violence,1,1,0,R,ONS,212/350 
Risky Move,1,1,0,R,ONS,223/350 
Aven Brigadier,1,1,0,R,ONS,7/350 
Aven Brigadier (premium),1,1,0,R,ONS,7/350
 */
public class MtgoImportDelegate extends CsvImportDelegate {
	public ReportType getType() {
		return ReportType.createReportType("mtgo", "Magic the Gathering Online");
	}

	public MtgoImportDelegate() {
		ICardField fields[] = new ICardField[7];
		fields[0] = MagicCardField.NAME;
		fields[5] = MagicCardField.EDITION_ABBR;
		setFields(fields);
	}

	@Override
	protected synchronized MagicCardPhisical createCard(List<String> list) {
		MagicCardPhisical x = super.createCard(list);
		try {
			String comment = "";
			if (list.get(1).equals("1"))
				comment += "online,";
			if (list.get(2).equals("1"))
				comment += "for trade,";
			if (list.get(0).endsWith(" (premium)")) {
				comment += "premium,";
			}
			comment += list.get(6);
			x.setComment(comment);
		} catch (Exception e) {
			Activator.log(e);
		}
		return x;
	}

	@Override
	protected void setFieldValue(MagicCardPhisical card, ICardField field, int i, String value) {
		if (i == 0 && value.endsWith(" (premium)")) {
			value = value.replaceAll("\\Q (premium)", "");
		}
		super.setFieldValue(card, field, i, value);
	}

	@Override
	protected void setHeaderFields(List<String> list) {
		// ignore header in a file
	}
}
