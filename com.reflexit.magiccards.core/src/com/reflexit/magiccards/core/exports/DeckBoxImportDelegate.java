package com.reflexit.magiccards.core.exports;

import java.util.List;

import com.reflexit.magiccards.core.exports.DeckBoxExportDelegate.ExtraFields;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;

public class DeckBoxImportDelegate extends CsvImportDelegate {
	public DeckBoxImportDelegate() {
	}

	/*-
	 Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number
	 1,0,Angel of Mercy,,,,,,Near Mint,English,
	 3,0,Platinum Angel,,,,,Magic 2010,Near Mint,English,218
	 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13
	 */
	@Override
	protected void setHeaderFields(List<String> list) {
		ICardField fields[] = new ICardField[] {
				MagicCardField.COUNT,
				MagicCardField.FORTRADECOUNT,
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

	public boolean isHeader() {
		return true;
	}
}
