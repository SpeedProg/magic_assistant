package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCardField;

public class LegalityColumn extends GenColumn {
	public LegalityColumn() {
		super(MagicCardField.LEGALITY, "Legality");
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			LegalityMap map = card.getLegalityMap();
			if (map.isEmpty()) {
				return "Unknown. Go to Legality tab and update latest legality of this card from internet.";
			}
			return "Legality column shows the newest\n" //
					+ "legality formats where card is legal,\n" //
					+ "(1) means restricted:\n" //
					+ "This card \"" + card.getName() + "\":\n" + map.fullText();
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			LegalityMap map = card.getLegalityMap();
			return map.getLabel();
		}
		return null;
	}
}
