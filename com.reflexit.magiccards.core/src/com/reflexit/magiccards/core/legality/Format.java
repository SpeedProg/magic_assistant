package com.reflexit.magiccards.core.legality;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.reflexit.magiccards.core.NotNull;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

public class Format {
	public static final Format STANDARD = new ConstructedFormat("Standard", 1);
	public static final Format EXTENDED = new ConstructedFormat("Extended", 2);
	public static final Format MODERN = new ConstructedFormat("Modern", 3);
	public static final Format LEGACY = new ConstructedFormat("Legacy", 4);
	public static final Format VINTAGE = new ConstructedFormat("Vintage", 5);
	public static final Format CLASSIC = new ConstructedFormat("Classic", 6);
	public static final Format FREEFORM = new ConstructedFormat("Freeform", 7);
	public static final int SAN_ORDINAL = 10;
	private final static LinkedHashMap<String, Format> formats = new LinkedHashMap<String, Format>();
	static {
		add(Format.STANDARD);
		add(Format.EXTENDED);
		add(Format.MODERN);
		add(Format.LEGACY);
		add(Format.VINTAGE);
		add(Format.CLASSIC);
		add(Format.FREEFORM);
	}
	@NotNull
	private final String name;
	private int ordinal = SAN_ORDINAL;

	public Format(String name) {
		this.name = name.intern();
	}

	private static void add(Format f) {
		formats.put(f.name(), f);
	}

	protected Format(String name, int ord) {
		this.name = name.intern();
		this.ordinal = ord;
	}

	public final static Collection<Format> getFormats() {
		return formats.values();
	}

	@Override
	public final int hashCode() {
		return 23 + name.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Format other = (Format) obj;
		if (!name.equals(other.name))
			return false;
		return true;
	}

	public int getMainDeckCount() {
		return -1;
	}

	public String name() {
		return name;
	}

	public int ordinal() {
		return ordinal;
	}

	@Override
	public String toString() {
		return name;
	}

	public static Format valueOf(String f) {
		Format real = formats.get(f);
		if (real != null)
			return real;
		Format format = new Format(f);
		add(format);
		return format;
	}

	public boolean checkLegality(ICardStore<IMagicCard> store, CardStoreUtils.CardStats stats) {
		if (!checkCardLegality(store))
			return false;
		return true;
	}

	protected boolean checkCardLegality(ICardStore<IMagicCard> store) {
		boolean res = true;
		for (IMagicCard card : store) {
			if (card instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) card;
				mcp.setError("");
				Legality leg = null;
				LegalityMap map = card.getLegalityMap();
				if (map != null) {
					leg = map.get(this);
				}
				if (leg == null) {
					leg = Legality.UNKNOWN;
				}
				if (!checkCardLegality(mcp, leg))
					res = false;
			}
		}
		return res;
	}

	public boolean isCountLegal(int count, Legality leg) {
		if (leg == Legality.RESTRICTED && count > 1)
			return false;
		return true;
	}

	protected boolean checkCardLegality(MagicCardPhysical mcp, Legality leg) {
		switch (leg) {
			case UNKNOWN:
				mcp.setError("Uknown legality for " + this);
				return false;
			case NOT_LEGAL:
				mcp.setError("Not legal for " + this);
				return false;
			case BANNED:
				mcp.setError("Banned for " + this);
				return false;
			case LEGAL:
				break;
			case RESTRICTED:
				if (isCountLegal(mcp.getCount(), leg)) {
					mcp.setError("Restricted for " + this + ". Only 1 allowed");
					return false;
				}
				break;
			default:
				break;
		}
		return true;
	}
}
