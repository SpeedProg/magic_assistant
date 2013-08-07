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

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * Export to Wagic: The Homebrew (http://wololo.net/wagic/) TODO: add description
 */
public class WagicExportDelegate extends AbstractExportDelegatePerLine<IMagicCard> {
	@Override
	public void printHeader() {
		stream.println("#NAME:" + getName());
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return false;
	}

	@Override
	public void printCard(IMagicCard card) {
		if (card instanceof MagicCardPhysical) {
			MagicCardPhysical physical = ((MagicCardPhysical) card);
			for (int i = 0; i < physical.getCount(); i++)
				stream.println(physical.getCardId());
		} else if (card instanceof MagicCard) {
			stream.println(card.getCardId());
		}
	}
}
