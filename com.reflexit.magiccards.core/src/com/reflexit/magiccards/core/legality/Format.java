package com.reflexit.magiccards.core.legality;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.reflexit.magiccards.core.NotNull;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

public class Format {
	public static final Format STANDARD = new ConstructedFormat("Standard", 1);
	public static final Format MODERN = new ConstructedFormat("Modern", 3);
	public static final Format LEGACY = new ConstructedFormat("Legacy", 4);
	public static final Format VINTAGE = new ConstructedFormat("Vintage", 5);
	public static final Format CLASSIC = new ConstructedFormat("Classic", 6);
	public static final Format FREEFORM = new Format("Freeform", 7);
	public static final int SAN_ORDINAL = 10;
	public static int ordcount = SAN_ORDINAL + 1;
	private final static LinkedHashMap<String, Format> formats = new LinkedHashMap<String, Format>();
	static {
		add(Format.STANDARD);
		add(Format.MODERN);
		add(Format.LEGACY);
		add(Format.VINTAGE);
		add(Format.CLASSIC);
		add(Format.FREEFORM);
		add(new CommanderFormat());
	}
	@NotNull
	private final String name;
	private int ordinal = SAN_ORDINAL;

	public Format(String name) {
		this.name = name.intern();
		this.ordinal = ordcount++;
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
		return 40;
	}

	public int getSideboardCount() {
		return 15;
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
		Format real = get(f);
		if (real != null)
			return real;
		Format format = new Format(f);
		add(format);
		return format;
	}

	public static Format get(String f) {
		Format real = formats.get(f);
		if (real == null && f.equals("Extended"))
			return MODERN;
		return real;
	}

	public String validateLegality(ICardStore<IMagicCard> store, CardStoreUtils.CardStats stats) {
		if (stats == null)
			return null;
		String err = validateStoreLegality(store);
		if (err != null)
			return err;
		if ((err = validateDeckCount(stats.mainCount)) != null)
			return err;
		if ((err = validateSideboardCount(stats.sideboardCount)) != null)
			return err;
		if ((err = validateCardCount(stats.maxRepeats)) != null)
			return err;
		return null;
	}

	protected String validateStoreLegality(ICardStore<IMagicCard> store) {
		for (IMagicCard card : store) {
			String err = validateCardLegality(card);
			if (err != null)
				return err;
		}
		for (IMagicCard card : store) {
			String err = validateCardCount(card);
			if (err != null)
				return err;
		}
		return null;
	}

	public Legality getLegality(IMagicCard card) {
		return card.getLegalityMap().mapOfLegality().get(this);
	}

	public String validateCardCount(IMagicCard card) {
		if (card.isBasicLand())
			return null;
		if (card instanceof ICardCountable) {
			int count = ((ICardCountable) card).getCount();
			Legality leg = getLegality(card);
			if (leg == Legality.RESTRICTED && count > 1)
				return "Restricted for " + this + ". Only 1 " + card.getName() + " is allowed";
			return validateCardCount(count);
		}
		return null;
	}

	/**
	 * @param count
	 *            - count of the cards to validate
	 */
	public String validateCardCount(int count) {
		return null;
	}

	public String validateCardOrGroup(IMagicCard element) {
		String err = validateCardLegality(element);
		if (err != null)
			return err;
		if (element instanceof CardGroup) {
			CardGroup group = (CardGroup) element;
			int count = group.getCount();
			ICardField field = group.getFieldIndex();
			if (field == MagicCardField.SIDEBOARD) {
				if (group.isSideboard()) {
					err = validateSideboardCount(count);
				} else {
					err = validateDeckCount(count);
				}
			} else if (field == MagicCardField.NAME) {
				err = validateCardCount(element);
			}
		} else {
			err = validateCardCount(element);
		}
		return err;
	}

	public String validateCardLegality(IMagicCard card) {
		Legality leg = getLegality(card);
		switch (leg) {
			case UNKNOWN:
				return ("Uknown legality for " + this);
			case NOT_LEGAL:
				return ("Not legal for " + this);
			case BANNED:
				return ("Banned for " + this);
			case LEGAL:
			case RESTRICTED:
				return null;
			default:
				return null;
		}
	}

	/**
	 * @param count
	 *            deck total cards
	 * @return error string or null if legal
	 */
	public String validateDeckCount(int count) {
		int min = getMainDeckCount();
		if (min == -1 || count >= min)
			return null;
		return "Deck card count is " + count + " expected >= " + min;
	}

	/**
	 * @param count
	 *            sideboard total cards
	 * @return error string or null if legal
	 */
	public String validateSideboardCount(int count) {
		int max = getSideboardCount();
		if (count <= max)
			return null;
		return "Sideboard card count is " + count + " expected <= " + max;
	}
}
