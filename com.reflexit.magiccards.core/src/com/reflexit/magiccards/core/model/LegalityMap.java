package com.reflexit.magiccards.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class LegalityMap extends LinkedHashMap<String, Legality> {
	public final static String formats[] = { "Standard", "Extended", "Modern", "Legacy", "Vintage", "Classic", "Freeform" };

	public LegalityMap() {
		// empty one
	}

	public LegalityMap(boolean init) {
		if (init) {
			for (String format : formats) {
				put(format, Legality.UNKNOWN);
			}
		}
	}

	public static String external(LegalityMap map) {
		String res = "";
		for (String format : formats) {
			Legality leg = map.get(format);
			if (leg == null)
				leg = Legality.NOT_LEGAL;
			res += format.charAt(0) + leg.getExt() + " ";
		}
		return res.trim();
	}

	public static Map<String, Legality> internal(String value) {
		Map<String, Legality> map = new HashMap<String, Legality>();
		String vs[] = value.split(" ");
		for (int i = 0; i < vs.length; i++) {
			String string = vs[i];
			map.put(formats[i], Legality.fromExt(string.substring(1)));
		}
		return map;
	}

	@Override
	public Legality put(String key, Legality value) {
		String format = key.intern();
		Legality prev = get(format);
		if (prev == null || value.ordinal() < prev.ordinal())
			return super.put(format, value);
		return prev;
	}

	public static LegalityMap calculateDeckLegality(Collection<LegalityMap> cardLegalities) {
		LegalityMap deckLegality = new LegalityMap(true);
		// all other formats that these cards mention
		for (LegalityMap cardLegalityRestrictions : cardLegalities) {
			for (Entry<String, Legality> cardLegalityEntry : cardLegalityRestrictions.entrySet()) {
				String formatForCard = cardLegalityEntry.getKey();
				deckLegality.put(formatForCard, Legality.UNKNOWN);
			}
		}
		for (LegalityMap cardLegalityRestrictions : cardLegalities) {
			updateDeckLegality(deckLegality, cardLegalityRestrictions);
		}
		return deckLegality;
	}

	private static void updateDeckLegality(LegalityMap deckLegality, LegalityMap cardLegality) {
		Set<String> cardFormats = cardLegality.keySet();
		// format not mentioned on the card legality map is illegal
		for (Entry<String, Legality> deckLegalityEntry : deckLegality.entrySet()) {
			String deckFormat = deckLegalityEntry.getKey();
			if (!cardFormats.contains(deckFormat)) {
				deckLegalityEntry.setValue(Legality.NOT_LEGAL);
			}
		}
		// update legality
		for (Entry<String, Legality> cardLegalityEntry : cardLegality.entrySet()) {
			String formatForCard = cardLegalityEntry.getKey();
			Legality formatLegality = cardLegalityEntry.getValue();
			deckLegality.put(formatForCard, formatLegality);
		}
	}
}
