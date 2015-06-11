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

import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * Pipe separated table
 */
public class TableExportDelegate extends AbstractExportDelegatePerLine<IMagicCard> {
	private final String SEP = "|";

	@Override
	public String getSeparator() {
		return SEP;
	}

	@Override
	protected String escape(String element) {
		if (element.contains(SEP)) {
			return element.replaceAll("\\Q" + SEP, "?");
		}
		return element;
	}

	@Override
	public String getExample() {
		return getTablePiped();
	}

	public static String getTablePiped() {
		return "NAME|ID|COST|TYPE|POWER|TOUGHNESS|SET|COUNT|SPECIAL|TEXT|SIDEBOARD\n"
				+ "Reya Dawnbringer|106384|{6}{W}{W}{W}|Legendary Creature - Angel|4|6|Tenth Edition|4||Flying<br>At the beginning of your upkeep, you may return target creature card from your graveyard to the battlefield.|false\n"
				+ "Platinum Angel|191313|{7}|Artifact Creature - Angel|4|4|Magic 2010|3|foil,c=good|Flying<br>You can't lose the game and your opponents can't win the game.|false\n"
				+ "Reya Dawnbringer|196998|{6}{W}{W}{W}|Legendary Creature - Angel|4|6|Duel Decks: Divine vs. Demonic|3|c=heavily_played|Flying<br>At the beginning of your upkeep, you may return target creature card from your graveyard to the battlefield.|false\n"
				+ "Reya Dawnbringer|196998|{6}{W}{W}{W}|Legendary Creature - Angel|4|6|Duel Decks: Divine vs. Demonic|1|c=heavily_played,fortrade|Flying<br>At the beginning of your upkeep, you may return target creature card from your graveyard to the battlefield.|false\n";
	}
}
