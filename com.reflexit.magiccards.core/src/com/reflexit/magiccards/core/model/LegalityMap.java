package com.reflexit.magiccards.core.model;

import java.util.Collection;
import java.util.LinkedHashMap;
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
		Legality prev = Legality.NOT_LEGAL;
		for (String format : formats) {
			Legality leg = map.get(format);
			if (leg == null)
				leg = Legality.NOT_LEGAL;
			if (leg != Legality.NOT_LEGAL && prev != Legality.LEGAL) {
				res += format + leg.getExt() + " ";
			}
			prev = leg;
		}
		return res.trim();
	}

	public static LegalityMap internal(String value) {
		LegalityMap map = new LegalityMap();
		String vs[] = value.split(" ");
		int i = 0;
		for (String format : formats) {
			if (i >= vs.length) {
				map.put(format, Legality.LEGAL);
			} else {
				String string = vs[i];
				if (string.startsWith(format)) {
					String ext = string.substring(format.length());
					Legality leg = Legality.fromExt(ext);
					map.put(format, leg);
				} else {
					map.put(formats[i], Legality.NOT_LEGAL);
				}
			}
			i++;
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

	public String legalFormats() {
		String res = "";
		if (isEmpty())
			return res;
		for (String format : keySet()) {
			if (get(format) == Legality.LEGAL) {
				res += format + ",";
			}
		}
		return res.substring(0, res.length() - 1);
	}

	public int compareTo(LegalityMap other) {
		if (other == null)
			return -1;
		String format1 = this.getFirstLegal();
		String format2 = other.getFirstLegal();
		if (format1 == format2)
			return 0;
		if (format1 == null)
			return 1;
		if (format2 == null)
			return -1;
		if (format1.equals(format2))
			return 0;
		int d = ordinal(format1) - ordinal(format2);
		return d;
	}

	public static int ordinal(String in) {
		if (in == null)
			return -1;
		int i = 0;
		for (String format : formats) {
			if (in.equals(format))
				return i;
			i++;
		}
		return -1;
	}

	private String getFirstLegal() {
		if (isEmpty())
			return null;
		for (String format : keySet()) {
			if (get(format) == Legality.LEGAL) {
				return format;
			}
		}
		return null;
	}
}
