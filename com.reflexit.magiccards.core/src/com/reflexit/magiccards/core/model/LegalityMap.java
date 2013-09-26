package com.reflexit.magiccards.core.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;

public class LegalityMap extends LinkedHashMap<String, Legality> {
	public final static String formats[] = { "Standard", "Extended", "Modern", "Legacy", "Vintage", "Classic", "Freeform" };
	public final static String SEP = "|";

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

	public String toExternal() {
		StringBuilder res = new StringBuilder();
		for (String format : keySet()) {
			Legality leg = get(format);
			if (leg == null)
				leg = Legality.NOT_LEGAL;
			res.append(format + leg.getExt() + SEP);
		}
		return res.toString();
	}

	public String getLabel() {
		StringBuilder res = new StringBuilder();
		Legality prev = Legality.NOT_LEGAL;
		for (String format : formats) {
			Legality leg = get(format);
			if (leg == null)
				leg = Legality.NOT_LEGAL;
			if (leg != Legality.NOT_LEGAL && leg != Legality.BANNED && prev != Legality.LEGAL) {
				res.append(format + leg.getExt() + " ");
			}
			prev = leg;
		}
		return res.toString().trim();
	}

	public String fullText() {
		String res = "";
		for (String format : formats) {
			Legality leg = get(format);
			res += format + leg.getExt() + " " + leg.getLabel() + "\n";
		}
		res += "\n";
		all: for (String format : keySet()) {
			Legality leg = get(format);
			if (leg == null)
				continue;
			if (leg == Legality.NOT_LEGAL)
				continue;
			for (String f : formats) {
				if (f.equals(format))
					continue all;
			}
			res += format + leg.getExt() + " " + leg.getLabel() + "\n";
		}
		return res.trim();
	}

	public static LegalityMap valueOf(String value) {
		LegalityMap map = new LegalityMap();
		String vs[] = value.split("\\Q" + SEP);
		for (int i = 0; i < vs.length; i++) {
			String string = vs[i];
			if (string == null || string.length() == 0)
				continue;
			String ext = string.substring(string.length() - 1, string.length());
			String f = string.substring(0, string.length() - 1);
			Legality leg = Legality.fromExt(ext);
			map.put(f, leg);
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

	public String getFirstLegal() {
		if (isEmpty())
			return null;
		for (String format : keySet()) {
			if (get(format) == Legality.LEGAL) {
				return format;
			}
		}
		return null;
	}

	public Legality getLegality(String format) {
		return get(format);
	}

	public void complete() {
		for (String format : formats) {
			if (get(format) == null)
				put(format, Legality.NOT_LEGAL);
			else
				break;
		}
	}
}
