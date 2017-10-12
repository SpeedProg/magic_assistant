package com.reflexit.magiccards.core.exports;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.reflexit.magiccards.core.exports.DeckBoxExportDelegate.ExtraFields;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class DeckBoxImportDelegate extends CsvImportDelegate {
	LinkedHashMap<String, ICardField> fieldMap = new LinkedHashMap<>();

	public DeckBoxImportDelegate() {
		fieldMap.put("Count", MagicCardField.COUNT);
		fieldMap.put("Tradelist Count", MagicCardField.FORTRADECOUNT);
		fieldMap.put("Name", MagicCardField.NAME);
		fieldMap.put("Foil", ExtraFields.FOIL);
		fieldMap.put("Textless", ExtraFields.TEXTLESS);
		fieldMap.put("Promo", ExtraFields.PROMO);
		fieldMap.put("Signed", ExtraFields.SIGNED);
		fieldMap.put("Edition", MagicCardField.SET);
		fieldMap.put("Condition", ExtraFields.CONDITION);
		fieldMap.put("Language", MagicCardField.LANG);
		fieldMap.put("Card Number", MagicCardField.COLLNUM);
		fieldMap.put("My Price", MagicCardField.PRICE);
	}

	public static String HEADER = "Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language";

	/*-
	 Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number
	 1,0,Angel of Mercy,,,,,,Near Mint,English,
	 3,0,Platinum Angel,,,,,Magic 2010,Near Mint,English,218
	 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13
	 
	 Count,Tradelist Count,Name,Edition,Card Number,Condition,Language,Foil,Signed,Artist Proof,Altered Art,Misprint,Promo,Textless,My Price,Type,Cost,Rarity
1,0,Ashes of the Abhorrent,Ixalan,2,Near Mint,Russian,,,,,,,,0,Enchantment,{1}{W},Rare
1,0,Bellowing Aegisaur,Ixalan,4,Near Mint,Russian,,,,,,,,0,Creature  - Dinosaur,{5}{W},Uncommon
2,0,Bishop's Soldier,Ixalan,6,Near Mint,Russian,,,,,,,,0,Creature  - Vampire Soldier,{1}{W},Common

	 */
	@Override
	protected void setHeaderFields(List<String> list) {
		ICardField fields[] = new ICardField[list.size()];
		int i = 0;
		for (Iterator iterator = list.iterator(); iterator.hasNext(); i++) {
			String name = (String) iterator.next();
			ICardField field = fieldMap.get(name);
			fields[i]=field;
			
		}
		
		setFields(fields);
	}

	@Override
	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field instanceof ExtraFields) {
			ExtraFields efield = (ExtraFields) field;
			efield.importInto(card, value);
			return;
		}
		if (field == null) return;
//		if (field == MagicCardField.NAME && value.contains("//")) {
//			value = value.replaceAll("(.*) // (.*)", "$1 // $2 ($1)");
//		}
		super.setFieldValue(card, field, i, value);
	}
}
