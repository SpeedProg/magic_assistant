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
import java.util.List;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/*-
  Format example

  "Name";"Qty";"Rarity";"Edition";"Color";"Cost";"P/T";"Type";"Mana";"Number";"Foil"
  "Disrupting Scepter";"1";"R";"4E";"Art";"3";"";"Artifact";"3";"316";""
  "Throne of Bone";"1";"U";"4E";"Art";"1";"";"Artifact";"1";"353";""
  "Cursed Rack";"1";"U";"4E";"Art";"4";"";"Artifact";"4";"312";""
  "Amulet of Kroog";"2";"C";"4E";"Art";"2";"";"Artifact";"2";"293";""
  "Strip Mine";"1";"U";"4E";"Lnd";"";"";"Land";"0";"363";""
  "Swamp (3)";"2";"C";"4E";"Lnd";"";"";"Land";"0";"378";""
  "Swamp (1)";"3";"C";"4E";"Lnd";"";"";"Land";"0";"376";""
 */
public class MagicWorkstationCsvImportDelegate extends CsvImportDelegate {
	@Override
	public char getSeparator() {
		return ';';
	}

	@Override
	public String getExample() {
		return ""
				+
				"\"Name\";\"Qty\";\"Rarity\";\"Edition\";\"Color\";\"Cost\";\"P/T\";\"Type\";\"Mana\";\"Number\";\"Foil\"\n"
				+
				"\"Disrupting Scepter\";\"1\";\"R\";\"4E\";\"Art\";\"3\";\"\";\"Artifact\";\"3\";\"316\";\"\"\n"
				+
				"\"Throne of Bone\";\"1\";\"U\";\"4E\";\"Art\";\"1\";\"\";\"Artifact\";\"1\";\"353\";\"\"\n"
				+
				"\"Cursed Rack\";\"1\";\"U\";\"4E\";\"Art\";\"4\";\"\";\"Artifact\";\"4\";\"312\";\"\"\n"
				+
				"\"Amulet of Kroog\";\"2\";\"C\";\"4E\";\"Art\";\"2\";\"\";\"Artifact\";\"2\";\"293\";\"\"\n"
				+
				"\"Strip Mine\";\"1\";\"U\";\"4E\";\"Lnd\";\"\";\"\";\"Land\";\"0\";\"363\";\"\"\n" +
				"\"Swamp (3)\";\"2\";\"C\";\"4E\";\"Lnd\";\"\";\"\";\"Land\";\"0\";\"378\";\"\"\n" +
				"\"Swamp (1)\";\"3\";\"C\";\"4E\";\"Lnd\";\"\";\"\";\"Land\";\"0\";\"376\";\"\"";
	}

	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		ICardField fields[] = new ICardField[7];
		fields[0] = MagicCardField.NAME;
		fields[1] = MagicCardField.COUNT;
		fields[3] = MagicCardField.EDITION_ABBR;
		setFields(fields);
		super.doRun(monitor);
	}

	@Override
	protected synchronized MagicCardPhysical createCard(List<String> list) {
		String name = list.get(0);
		if (name.length() == 0)
			return null;
		MagicCardPhysical x = super.createCard(list);
		try {
			if (list.get(10).equals("1")) {
				x.setSpecialTag("foil");
			}
			String comment = list.get(9);
			x.setComment(comment);
		} catch (Exception e) {
			throw new MagicException(e);
		}
		return x;
	}

	@Override
	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
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
