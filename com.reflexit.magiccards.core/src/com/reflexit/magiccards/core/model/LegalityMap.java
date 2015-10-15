package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class LegalityMap {
	private final static Collection<Format> formats = Format.getFormats();
	public final static String SEP = "|";
	public static final LegalityMap EMPTY = new LegalityMap();
	private final String external;

	private static class LegalityHashMap extends TreeMap<Format, Legality> {
		LegalityHashMap() {
			super(new Comparator<Format>() {
				@Override
				public int compare(Format f1, Format f2) {
					return f1.ordinal() - f2.ordinal();
				}
			});
		}

		LegalityHashMap(Map<Format, Legality> value) {
			this();
			putAll(value);
		}

		@Override
		public Legality put(Format key, Legality value) {
			// if (isEmpty())
			// initFormats();
			return super.put(key, value);
		}

		public LegalityMap toLegalityMap() {
			return LegalityMap.valueOf(toExternal());
		}

		@Override
		public Legality get(Object f) {
			if (f == null || isEmpty())
				return Legality.UNKNOWN;
			Legality x = null;
			if (f instanceof Format) {
				if (!super.containsKey(f))
					completeL();
				x = super.get(f);
			}
			else
				throw new IllegalArgumentException();
			if (x == null)
				return Legality.UNKNOWN;
			return x;
		}

		public String toExternal() {
			StringBuilder res = new StringBuilder();
			Legality prev = null;
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

		public LegalityHashMap completeL() {
			Legality leg = Legality.NOT_LEGAL;
			for (Format format : formats) {
				if (format.ordinal() >= Format.SAN_ORDINAL)
					break;
				Legality cur = super.get(format);
				if ((cur == Legality.UNKNOWN || cur == null) && leg == Legality.LEGAL) {
					put(format, leg);
				} else {
					leg = cur;
				}
			}
			return this;
		}

		public LegalityHashMap complete() {
			Legality leg = Legality.NOT_LEGAL;
			for (Format format : formats) {
				Legality cur = super.get(format);
				if (cur == null)
					cur = Legality.UNKNOWN;
				if (cur == Legality.UNKNOWN) {
					if (format.ordinal() >= Format.SAN_ORDINAL)
						put(format, Legality.NOT_LEGAL);
					else
						put(format, leg);
				} else {
					leg = cur;
				}
			}
			return this;
		}

		public Legality merge(Format format, Legality value) {
			Legality prev = get(format);
			if (prev == Legality.UNKNOWN || value.ordinal() < prev.ordinal())
				return put(format, value);
			return prev;
		}

		private static LegalityHashMap valueOf(String value) {
			LegalityHashMap map = new LegalityHashMap();
			if (value == null || value.trim().isEmpty())
				return map;
			String vs[] = value.split("\\Q" + SEP);
			for (int i = 0; i < vs.length; i++) {
				String string = vs[i];
				if (string == null || string.length() == 0)
					continue;
				try {
					Format format = Format.get(string);
					if (format == null) {
						if (string.equals("*")) {
							map.put(Format.LEGACY, Legality.LEGAL);
						} else {
							String ext = string.substring(string.length() - 1, string.length());
							if (Legality.isExt(ext)) {
								Legality leg = Legality.fromExt(ext);
								String f = string.substring(0, string.length() - 1);
								format = Format.valueOf(f);
								map.put(format, leg);
							} else {
								format = Format.valueOf(string);
								map.put(format, Legality.LEGAL);
							}
						}
					} else {
						map.put(format, Legality.LEGAL);
					}
				} catch (Exception e) {
					MagicLogger.log(e); // move on
				}
			}
			return map;
		}
	}

	private LegalityMap() {
		// empty one
		external = "";
	}

	private LegalityMap(String string) {
		external = string;
	}

	public String toExternal() {
		return external;
	}

	@Override
	public String toString() {
		return external;
	}

	public String getLabel() {
		Format f = getFirstLegal();
		Legality leg = getFromFormat(f);
		if (leg == Legality.LEGAL)
			return f.name();
		// otherwise restricted
		if (leg == Legality.RESTRICTED)
			return f.name() + " (1)";
		// constructed
		return f.name();
	}

	public String fullText() {
		LegalityHashMap map = map().complete();
		String res = "";
		for (Format format : map.keySet()) {
			Legality leg = map.get(format);
			if ((format.ordinal() >= Format.SAN_ORDINAL)
					&& (leg == Legality.NOT_LEGAL || leg == Legality.UNKNOWN))
				continue;
			res += format + " - " + leg.getLabel() + "\n";
		}
		return res.trim();
	}

	public static LegalityMap valueOf(Object value) {
		if (value == null)
			return EMPTY;
		if (value instanceof LegalityMap)
			return (LegalityMap) value;
		if (value instanceof String) {
			String str = (String) value;
			if (str.trim().isEmpty())
				return EMPTY;
			return new LegalityMap(str);
		}
		if (value instanceof Map) {
			return new LegalityHashMap((Map) value).toLegalityMap();
		}
		if (value instanceof Format) {
			return new LegalityMap().put((Format) value, Legality.LEGAL);
		}
		throw new IllegalArgumentException();
	}

	public LegalityMap merge(LegalityMap source) {
		LegalityHashMap map = map();
		merge(map, source.map());
		return map.toLegalityMap();
	}

	private static void merge(LegalityHashMap dest, LegalityHashMap source) {
		for (Entry<Format, Legality> cardLegalityEntry : source.entrySet()) {
			Format formatForCard = cardLegalityEntry.getKey();
			Legality formatLegality = cardLegalityEntry.getValue();
			dest.merge(formatForCard, formatLegality);
		}
	}

	public Legality get(Object f) {
		if (f == null || isEmpty())
			return Legality.UNKNOWN;
		Legality x = null;
		if (f instanceof Format)
			x = getFromFormat((Format) f);
		else if (f instanceof String)
			x = getFromFormat(Format.valueOf((String) f));
		else
			throw new IllegalArgumentException();
		if (x == null)
			return Legality.UNKNOWN;
		return x;
	}

	public Map<Format, Legality> mapOfLegality() {
		return LegalityHashMap.valueOf(external).complete();
	}

	LegalityHashMap map() {
		return LegalityHashMap.valueOf(external);
	}

	private Legality getFromFormat(Format f) {
		return map().get(f);
	}

	public static LegalityMap calculateDeckLegality(ICardStore<IMagicCard> store) {
		Collection<LegalityMap> cardLegalities = new ArrayList<LegalityMap>();
		for (IMagicCard card : store) {
			cardLegalities.add(card.getLegalityMap());
		}
		return calculateDeckLegality(cardLegalities);
	}

	public static LegalityMap calculateDeckLegality(Collection<LegalityMap> cardLegalities) {
		LegalityHashMap deckLegality = new LegalityHashMap();
		// all other formats that these cards mention
		for (LegalityMap cardLegalityRestrictions : cardLegalities) {
			LegalityHashMap sourceMap = cardLegalityRestrictions.map().completeL();
			deckLegality.putAll(sourceMap);
		}
		for (LegalityMap cardLegalityRestrictions : cardLegalities) {
			LegalityHashMap sourceMap = cardLegalityRestrictions.map().completeL();
			updateDeckLegality(deckLegality, sourceMap);
		}
		return deckLegality.toLegalityMap();
	}

	private static void updateDeckLegality(LegalityHashMap deckLegality, LegalityHashMap map) {
		Set<Format> cardFormats = map.keySet();
		// format not mentioned on the card legality map is illegal
		for (Entry<Format, Legality> deckLegalityEntry : deckLegality.entrySet()) {
			Format deckFormat = deckLegalityEntry.getKey();
			if (!cardFormats.contains(deckFormat)) {
				deckLegalityEntry.setValue(Legality.NOT_LEGAL);
			}
		}
		// update legality
		merge(deckLegality, map);
	}

	public String legalFormats() {
		String res = "";
		if (isEmpty())
			return res;
		LegalityHashMap map = map();
		for (Format format : map.keySet()) {
			if (map.get(format) == Legality.LEGAL) {
				res += format + ",";
			}
		}
		if (res.length() == 0)
			return res;
		return res.substring(0, res.length() - 1);
	}

	public boolean isEmpty() {
		return external.isEmpty();
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
		LegalityHashMap map = map();
		for (Format format : map.keySet()) {
			if (format.ordinal() < Format.SAN_ORDINAL) {
				Legality leg = map.get(format);
				if (leg == Legality.LEGAL || leg == Legality.RESTRICTED) {
					return format;
				}
			}
		}
		if (!map.containsKey(Format.LEGACY))
			return Format.LEGACY;
		return Format.FREEFORM;
	}

	public boolean isLegal(Format format) {
		return get(format) == Legality.LEGAL;
	}

	@Override
	public int hashCode() {
		return toExternal().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof LegalityMap))
			return false;
		LegalityMap other = (LegalityMap) obj;
		String x = toExternal();
		if (!x.equals(other.toExternal()))
			return false;
		return true;
	}

	/**
	 * @param value
	 *            Comma separated list of legal formats
	 * @return
	 */
	public static LegalityMap createFromLegal(String value) {
		if (value == null || value.trim().length() == 0)
			return LegalityMap.EMPTY;
		LegalityHashMap legalityMap = new LegalityHashMap();
		String[] formatss = value.trim().split(",");
		for (int i = 0; i < formatss.length; i++) {
			String string = formatss[i];
			legalityMap.put(Format.valueOf(string.trim()), Legality.LEGAL);
		}
		legalityMap.complete();
		return legalityMap.toLegalityMap();
	}

	public LegalityMap put(Format format, Legality legal) {
		LegalityHashMap map = map();
		map.put(format, legal);
		return map.toLegalityMap();
	}

	public LegalityMap merge(Format format, Legality legal) {
		LegalityHashMap map = map();
		map.merge(format, legal);
		return map.toLegalityMap();
	}

	/**
	 * All unknown legalities become illegal
	 * 
	 * @return
	 */
	public LegalityMap complete() {
		return map().complete().toLegalityMap();
	}
}
