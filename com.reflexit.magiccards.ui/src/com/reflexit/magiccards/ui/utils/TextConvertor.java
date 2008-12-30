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

import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * @author Alena
 *
 */
public class TextConvertor {
	public static String toText(IMagicCard card) {
		StringBuffer buf = new StringBuffer();
		buf.append(card.getName());
		buf.append(" ");
		buf.append(card.getCost());
		buf.append("\n");
		buf.append(card.getType());
		buf.append(" [");
		buf.append(card.getSet() + " - " + card.getRarity());
		buf.append("]");
		buf.append("\n");
		buf.append(card.getOracleText());
		buf.append("\n");
		return buf.toString();
	}
}
