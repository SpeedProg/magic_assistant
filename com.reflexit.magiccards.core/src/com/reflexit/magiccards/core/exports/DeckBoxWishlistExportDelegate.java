package com.reflexit.magiccards.core.exports;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;

public class DeckBoxWishlistExportDelegate extends DeckBoxExportDelegate {
	@Override
	public void printHeader() {
		stream.println("Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number");
	}

	@Override
	protected ICardField[] doGetFields() {
		ICardField fields[] = new ICardField[] {
				MagicCardField.COUNT,
				MagicCardField.NAME,
				ExtraFields.FOIL,
				ExtraFields.TEXTLESS,
				ExtraFields.PROMO,
				ExtraFields.SIGNED,
				MagicCardField.SET,
				ExtraFields.CONDITION,
				MagicCardField.LANG,
				MagicCardField.COLLNUM
		};
		return fields;
	}
}
