package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class DeckBoxExportDelegate extends CsvExportDelegate {
	/*-
	 Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number
	 1,0,Angel of Mercy,,,,,,Near Mint,English,
	 3,0,Platinum Angel,,,,,Magic 2010,Near Mint,English,218
	 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13
	 */
	@Override
	public void printHeader() {
		stream.println("Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number");
	}

	protected ICardField[] doGetFields() {
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
		return fields;
	}

	@Override
	public void run(ICoreProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		setColumns(doGetFields());
		super.run(monitor);
	}
	
	@Override
	public Object getObjectByField(IMagicCard card, ICardField field) {
		return field.aggregateValueOf(card);
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return false;
	}
	

	public enum ExtraFields implements ICardField {
		FOIL,
		TEXTLESS,
		PROMO,
		SIGNED,
		CONDITION {
			@Override
			public Object aggregateValueOf(ICard card) {
				String spe = card.getString(MagicCardField.SPECIAL);
				if (spe.contains("nearmint")) return "Near Mint";
				if (spe.contains("mint")) return "Mint";
				if (spe.contains("played")) return "Played";
				return "";
			}
		};

		@Override
		public boolean isTransient() {
			return false;
		}

		@Override
		public String getLabel() {
			String name = name();
			name = name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
			name = name.replace('_', ' ');
			return name;
		}

		@Override
		public Object aggregateValueOf(ICard card) {
			String spe = card.getString(MagicCardField.SPECIAL);
			if (spe.contains(getTag())) return getTag();
			return "";
		}
		

		@Override
		public String getTag() {
			String name = name();
			name = name.toLowerCase(Locale.ENGLISH);
			return name;
		}
		
	}
}
