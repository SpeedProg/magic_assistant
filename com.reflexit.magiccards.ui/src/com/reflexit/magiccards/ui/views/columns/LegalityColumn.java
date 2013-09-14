package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class LegalityColumn extends GenColumn {
	public LegalityColumn() {
		super(MagicCardField.LEGALITY, "Legality");
	}

	@Override
	public String getToolTipText(Object element) {
		MagicCard base;
		if (element instanceof IMagicCard) {
			base = (MagicCard) ((IMagicCard) element).getBase();
		} else if (element instanceof CardGroup) {
			base = (MagicCard) ((CardGroup) element).getBase();
		} else
			return null;
		LegalityMap map = base.getLegalityMap();
		if (map == null || map.size() == 0) {
			return "Unknown. Go to Legality tab and update latest legality of this card from internet.";
		}
		return "Shows the newest legality formats where card is legal, with the following modifiers:\n + Legal, 1 Restricted, ! Banned, - Illegal\nThis card:\n"
				+ map.fullText();
	}
}
