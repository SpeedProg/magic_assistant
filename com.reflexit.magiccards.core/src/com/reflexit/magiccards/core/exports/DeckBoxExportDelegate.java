package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.SpecialTags;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
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
		if (field == MagicCardField.LANG && card.getLanguage() == null)
			return "English";
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
				if (card instanceof MagicCardPhysical) {
					String spe = ((MagicCardPhysical) card).getSpecialTagValue("c");
					if (spe == null)
						return null;
					if (spe.equals("nearmint"))
						return "Near Mint";
					if (spe.equals("good"))
						return "Good (Lightly Played)";
					if (spe.equals("mint"))
						return "Mint";
					if (spe.equals("heavily_played"))
						return "Heavily Played";
					if (spe.equals("poor"))
						return "Poor";
					return spe;
				}
				return "";
			}

			@Override
			public void importInto(MagicCardPhysical card, String value) {
				String tag = null;
				if (value.equals("Near Mint"))
					tag = "nearmint";
				else if (value.startsWith("Good"))
					tag = "good";
				else {
					tag = value.toLowerCase().replaceAll("\\W", "_");
				}
				if (tag != null)
					card.setSpecialTag("c=" + tag);
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

		public String getTag() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public Object aggregateValueOf(ICard card) {
			if (SpecialTags.getInstance().toMap((IMagicCard) card).containsKey(getTag()))
				return getTag();
			return "";
		}

		public void importInto(MagicCardPhysical card, String value) {
			String tag = getTag();
			if (value.equals(tag)) {
				card.setSpecialTag(tag);
			} else {
				card.removeSpecialTag(tag);
			}
		}
	}
}
