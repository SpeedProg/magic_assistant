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
package com.reflexit.magiccards.core.test.assist;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * @author Alena
 *
 */
public class CardGenerator {
	static int id = 33;

	static public MagicCard generateRandomCard() {
		MagicCard card = new MagicCard();
		card.setCardId(++id);
		return card;
	}

	static public MagicCard generateCardWithValues() {
		try {
			MagicCard card = new MagicCard();
			card.setCardId(id);
			card.setName("name " + id);
			card.setType("type " + id % 100);
			card.setCost("{" + (id % 7) + "}");
			card.setOracleText("bla " + id);
			card.setSet("set " + (id % 20));
			return card;
		} finally {
			id++;
		}
	}

	static public MagicCardPhisical generatePhysicalCardWithValues() {
		MagicCard card = generateCardWithValues();
		MagicCardPhisical phi = new MagicCardPhisical(card, null);
		phi.setCount(id % 10);
		phi.setComment("comment " + id);
		phi.setOwn(id % 2 == 0 ? true : false);
		return phi;
	}
}
