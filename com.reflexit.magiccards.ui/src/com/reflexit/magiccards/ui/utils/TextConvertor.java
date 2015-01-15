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
package com.reflexit.magiccards.ui.utils;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;

/**
 * @author Alena
 * 
 */
public class TextConvertor {
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static String toText(Object line) {
		StringBuffer buf = new StringBuffer();
		IMagicCard card;
		if (line instanceof ICardGroup) {
			buf.append(((CardGroup) line).getName());
		} else if (line instanceof IMagicCard) {
			card = (IMagicCard) line;
			buf.append(card.getName());
			buf.append(" ");
			buf.append(card.getCost());
			buf.append(LINE_SEPARATOR);
			buf.append(card.getType());
			buf.append(" [");
			buf.append(card.getSet() + " - " + card.getRarity());
			buf.append("]");
			buf.append(LINE_SEPARATOR);
			buf.append(card.getOracleText());
		} else {
			buf.append(line.toString());
		}
		buf.append(LINE_SEPARATOR);
		buf.append("--------------------------");
		buf.append(LINE_SEPARATOR);
		return buf.toString();
	}
}
