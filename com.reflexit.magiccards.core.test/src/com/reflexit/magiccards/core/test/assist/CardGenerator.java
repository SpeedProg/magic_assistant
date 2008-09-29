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
}
