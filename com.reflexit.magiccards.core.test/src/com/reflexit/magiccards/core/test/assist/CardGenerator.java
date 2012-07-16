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

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.Rarity;

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
			card.setRarity(Rarity.COMMON);
			card.setType("type " + id % 100);
			card.setCost("{" + (id % 7) + "}");
			card.setOracleText("bla " + id);
			card.setSet("set " + (id % 20));
			card.setText("bla <br> bla " + id);
			card.setPower(String.valueOf(id % 5));
			card.setToughness("*");
			card.setCollNumber(id + "a");
			card.setObjectByField(MagicCardField.RATING, "2." + id);
			card.setDbPrice(1.2f + 1 / (float) id);
			card.setArtist("Elena " + id);
			card.setLanguage("Russian");
			return card;
		} finally {
			id++;
		}
	}

	static public MagicCardPhysical generatePhysicalCardWithValues() {
		MagicCard card = generateCardWithValues();
		MagicCardPhysical phi = new MagicCardPhysical(card, null);
		phi.setCount(id % 10);
		phi.setComment("comment " + id);
		phi.setOwn(id % 2 == 0 ? true : false);
		return phi;
	}

	static public MagicCardPhysical generatePhysicalCardWithValues(IMagicCard card) {
		MagicCardPhysical phi = new MagicCardPhysical(card, null);
		phi.setCount(id % 10);
		phi.setComment("comment " + id);
		phi.setOwn(id % 2 == 0 ? true : false);
		phi.setSpecial("mint");
		return phi;
	}
}
