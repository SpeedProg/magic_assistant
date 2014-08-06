/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *    Terry Long - refactored ParseGathererLegality to instead retrieve rulings on cards
 *
 *******************************************************************************/
package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Retrieve legality info
 */
public class ParseGathererPrinted extends ParseGathererOracle {
	public ParseGathererPrinted() {
		// Set<ICardField> filter = new HashSet<ICardField>();
		// filter.add(MagicCardField.NAME);
		// filter.add(MagicCardField.TYPE);
		// filter.add(MagicCardField.TEXT);
		// setFilter(filter);
	}

	@Override
	protected String getUrl() {
		return super.getUrl() + "&printed=true";
	}

	@Override
	protected boolean isOracle() {
		return false;
	}

	public static void main(String[] args) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(172550);
		// card.setCardId(191338);
		ParseGathererPrinted parser = new ParseGathererPrinted();
		parser.setCard(card);
		parser.load(ICoreProgressMonitor.NONE);
		System.out.println(card.getName());
		System.out.println(card.getType());
		System.out.println(card.getOracleText());
	}
}
