package com.reflexit.magiccards.core.exports;

import java.util.List;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.exports.DeckBoxExportDelegate.ExtraFields;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.sync.TextPrinter;

public class DeckBoxWishlistImportDelegate extends DeckBoxImportDelegate {
	public static String HEADER_W = "Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language";

	@Override
	protected void setHeaderFields(List<String> list) {
		String header = TextPrinter.join(list, ',');
		if (!header.startsWith(HEADER_W))
			throw new MagicException("Expecting header: " + HEADER_W);
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
