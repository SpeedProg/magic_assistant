package com.reflexit.magiccards.core.exports;

import java.util.List;

import com.reflexit.magiccards.core.exports.DeckBoxExportDelegate.ExtraFields;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;

public class DeckBoxWishlistImportDelegate extends DeckBoxImportDelegate {
	@Override
	protected void setHeaderFields(List<String> list) {
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
		setFields(fields);
	}
}
