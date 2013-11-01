package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class LegalityMap extends LinkedHashMap<Format, Legality> {
	private final static Collection<Format> formats = Format.getFormats();
	public final static String SEP = "|";

	public LegalityMap() {
		// empty one
	}

	protected void initFormats() {
		for (Format format : formats) {
			if (format.ordinal() < Format.SAN_ORDINAL)
				super.put(format, Legality.UNKNOWN);
		}
	}

	public String toExternal() {
		StringBuilder res = new StringBuilder();
		Legality prev = Legality.NOT_LEGAL;
		for (Format format : keySet()) {
			Legality leg = get(format);
			if (format.ordinal() <= Format.SAN_ORDINAL) {
				if (leg == prev) {
					continue;
				}
				prev = leg;
			}
			if (leg == Legality.UNKNOWN) {
				continue;
			}
			if (leg == Legality.LEGAL)
				res.append(format + SEP);
			else
				res.append(format + leg.getExt() + SEP);
		}
		String sres = res.toString();
		if (sres.length() > 0)
			return sres.substring(0, sres.length() - 1);
		return "";
	}

	@Override
	public String toString() {
		return toExternal();
	}

	public String getLabel() {
		Format f = getFirstLegal();
		Legality leg = get(f);
		if (leg == Legality.LEGAL)
			return f.name();
		// otherwise restricted
		if (leg == Legality.RESTRICTED)
			return f.name() + " (1)";
		// constructed
		return f.name();
	}

	public String fullText() {
		String res = "";
		for (Format format : keySet()) {
			Legality leg = get(format);
			if ((format.ordinal() >= Format.SAN_ORDINAL) && (leg == Legality.NOT_LEGAL || leg == Legality.UNKNOWN))
				continue;
			res += format + " - " + leg.getLabel() + "\n";
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
			try {
				Format format = Format.get(string);
				if (format == null) {
					String ext = string.substring(string.length() - 1, string.length());
					String f = string.substring(0, string.length() - 1);
					Legality leg = Legality.fromExt(ext);
					format = Format.valueOf(f);
					map.put(format, leg);
				} else {
					map.put(format, Legality.LEGAL);
				}
			} catch (Exception e) {
				MagicLogger.log(e); // move on
			}
		}
		return map;
	}

	@Override
	public Legality put(Format format, Legality value) {
		if (isEmpty())
			initFormats();
		return super.put(format, value);
	}

	public Legality merge(Format format, Legality value) {
		Legality prev = get(format);
		if (prev == Legality.UNKNOWN || value.ordinal() < prev.ordinal())
			return super.put(format, value);
		return prev;
	}

	@Override
	public Legality get(Object f) {
		Legality x = null;
		if (f instanceof Format)
			x = super.get(f);
		else if (f instanceof String)
			x = super.get(Format.valueOf((String) f));
		else if (f == null)
			return Legality.UNKNOWN;
		else
			throw new IllegalArgumentException();
		if (x == null)
			return Legality.UNKNOWN;
		return x;
	}

	public static LegalityMap calculateDeckLegality(ICardStore<IMagicCard> store) {
		Collection<LegalityMap> cardLegalities = new ArrayList<LegalityMap>();
		for (IMagicCard card : store) {
			LegalityMap map = card.getLegalityMap();
			if (map != null)
				cardLegalities.add(map);
		}
		return calculateDeckLegality(cardLegalities);
	}

	public static LegalityMap calculateDeckLegality(Collection<LegalityMap> cardLegalities) {
		LegalityMap deckLegality = new LegalityMap();
		// all other formats that these cards mention
		for (LegalityMap cardLegalityRestrictions : cardLegalities) {
			for (Entry<Format, Legality> cardLegalityEntry : cardLegalityRestrictions.entrySet()) {
				Format formatForCard = cardLegalityEntry.getKey();
				deckLegality.put(formatForCard, Legality.UNKNOWN);
			}
		}
		for (LegalityMap cardLegalityRestrictions : cardLegalities) {
			updateDeckLegality(deckLegality, cardLegalityRestrictions);
		}
		return deckLegality;
	}

	private static void updateDeckLegality(LegalityMap deckLegality, LegalityMap cardLegality) {
		Set<Format> cardFormats = cardLegality.keySet();
		// format not mentioned on the card legality map is illegal
		for (Entry<Format, Legality> deckLegalityEntry : deckLegality.entrySet()) {
			Format deckFormat = deckLegalityEntry.getKey();
			if (!cardFormats.contains(deckFormat)) {
				deckLegalityEntry.setValue(Legality.NOT_LEGAL);
			}
		}
		// update legality
		for (java.util.Map.Entry<Format, Legality> cardLegalityEntry : cardLegality.entrySet()) {
			Format formatForCard = cardLegalityEntry.getKey();
			Legality formatLegality = cardLegalityEntry.getValue();
			deckLegality.merge(formatForCard, formatLegality);
		}
	}

	public String legalFormats() {
		String res = "";
		if (isEmpty())
			return res;
		for (Format format : keySet()) {
			if (get(format) == Legality.LEGAL) {
				res += format + ",";
			}
		}
		if (res.length() == 0)
			return res;
		return res.substring(0, res.length() - 1);
	}

	public int compareTo(LegalityMap other) {
		if (other == null)
			return -1;
		Format format1 = this.getFirstLegal();
		Format format2 = other.getFirstLegal();
		if (format1 == format2)
			return 0;
		if (format1 == null)
			return 1;
		if (format2 == null)
			return -1;
		if (format1.equals(format2))
			return 0;
		int d = format1.ordinal() - format2.ordinal();
		return d;
	}

	public Format getFirstLegal() {
		if (isEmpty())
			return Format.LEGACY;
		for (Format format : keySet()) {
			if (format.ordinal() < Format.SAN_ORDINAL) {
				Legality leg = get(format);
				if (leg == Legality.LEGAL || leg == Legality.RESTRICTED) {
					return format;
				}
			}
		}
		if (!containsKey(Format.LEGACY))
			return Format.LEGACY;
		return Format.FREEFORM;
	}

	public void complete() {
		Legality leg = Legality.NOT_LEGAL;
		for (Format format : formats) {
			Legality cur = get(format);
			if (cur == Legality.UNKNOWN) {
				if (format.ordinal() >= Format.SAN_ORDINAL)
					put(format, Legality.NOT_LEGAL);
				else
					put(format, leg);
			} else {
				leg = cur;
			}
		}
	}

	public boolean isLegal(Format format) {
		return get(format) == Legality.LEGAL;
	}
}
