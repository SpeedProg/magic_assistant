/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Wing�rd  - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import java.net.URL;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.sync.GatherHelper;

public class HtmlTableExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public HtmlTableExportDelegate() {
	}

	public ReportType getType() {
		return ReportType.createReportType("Magic Assistant HTML Table");
	}

	@Override
	public void printCard(IMagicCard card) {
		String abbr = Editions.getInstance().getAbbrByName(card.getSet());
		String line = "<tr>";
		for (int i = 0; i < columns.length; i++) {
			ICardField field = columns[i];
			Object value = card.getObjectByField(field);
			if (value == null)
				value = "";
			if (field == MagicCardField.COST) {
				String str = String.valueOf(value);
				if (str.length() > 0)
					value = replaceSymbolsWithLinksOnline(str);
			}
			if (field == MagicCardField.NAME) {
				value = img(GatherHelper.createSetImageURL(abbr, card.getRarity()), "") + "&nbsp;" + value;
			}
			line += ("<td>" + value + "</td>");
		}
		stream.println(line + "</tr>");
	}

	@Override
	public void printFooter() {
		stream.println("</table>");
		stream.println("</body></html>");
	}

	@Override
	public void printLocationHeader() {
		stream.println("<tr>");
		stream.println("<td colspan=" + columns.length + "><h2>" + location.getName() + "</h2></td>");
		stream.println("</tr>");
	}

	@Override
	public void printHeader() {
		stream.println("<html><head><title>" + getName() + "</title></head><body>");
		stream.println("<table>");
		String line = "<tr>";
		for (int i = 0; i < columns.length; i++) {
			ICardField field = columns[i];
			String name;
			if (field == MagicCardField.POWER)
				name = "P";
			else if (field == MagicCardField.TOUGHNESS)
				name = "T";
			else
				name = field.name();
			line += "<th>" + name + "</th>";
		}
		stream.println(line + "</tr>");
	}

	private String replaceSymbolsWithLinksOnline(String cost) {
		String text = "";
		String[] manasplit = Colors.getInstance().manasplit(cost);
		for (int i = 0; i < manasplit.length; i++) {
			String mana = manasplit[i];
			String mana1 = mana.replaceAll("/", "");
			text += img(GatherHelper.createManaImageURL(mana1), "{" + mana + "}");
		}
		return text;
	}

	private String img(URL createSetImageURL, String alt) {
		return "<img  style=\"float:left\"  src=\"" + createSetImageURL + "\" alt=\"" + alt + "\"/>";
	}
}
