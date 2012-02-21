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

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * Format example "Name";"Qty";"Rarity";"Edition";"Color";"Cost";"P/T";"Type";"Mana";"Number";"Foil"
 * "Disrupting Scepter";"1";"R";"4E";"Art";"3";"";"Artifact";"3";"316";""
 * "Throne of Bone";"1";"U";"4E";"Art";"1";"";"Artifact";"1";"353";""
 * "Cursed Rack";"1";"U";"4E";"Art";"4";"";"Artifact";"4";"312";""
 * "Amulet of Kroog";"2";"C";"4E";"Art";"2";"";"Artifact";"2";"293";""
 * "Strip Mine";"1";"U";"4E";"Lnd";"";"";"Land";"0";"363";""
 * "Swamp (3)";"2";"C";"4E";"Lnd";"";"";"Land";"0";"378";""
 * "Swamp (1)";"3";"C";"4E";"Lnd";"";"";"Land";"0";"376";""
 */
public class MagicWorkstationImportDelegate extends CsvImportDelegate {
	@Override
	public ReportType getType() {
		return ReportType.createReportType("mwcsv", "Magic Workstation CSV");
	}

	public MagicWorkstationImportDelegate() {
		ICardField fields[] = new ICardField[7];
		fields[0] = MagicCardField.NAME;
		fields[1] = MagicCardFieldPhysical.COUNT;
		fields[3] = MagicCardField.EDITION_ABBR;
		setFields(fields);
	}

	@Override
	public char getSeparator() {
		return ';';
	}

	@Override
	protected synchronized MagicCardPhisical createCard(List<String> list) {
		String name = list.get(0);
		if (name.length() == 0)
			return null;
		MagicCardPhisical x = super.createCard(list);
		try {
			String comment = "";
			if (list.get(10).equals("1"))
				comment += "foil,";
			comment += list.get(9);
			x.setComment(comment);
		} catch (Exception e) {
			MagicLogger.log(e);
		}
		return x;
	}

	@Override
	protected void setFieldValue(MagicCardPhisical card, ICardField field, int i, String value) {
		if (i == 0 && value.endsWith(")")) {
			value = value.replaceAll(" \\(\\d+\\)$", "");
		}
		super.setFieldValue(card, field, i, value);
	}

	@Override
	protected void setHeaderFields(List<String> list) {
		// ignore header in a file
	}
}
