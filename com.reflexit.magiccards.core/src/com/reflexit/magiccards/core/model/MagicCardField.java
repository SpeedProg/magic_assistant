package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardVisitor;
import com.reflexit.magiccards.core.model.aggr.AbstractFloatCountAggregator;
import com.reflexit.magiccards.core.model.aggr.AbstractIntTransAggregator;
import com.reflexit.magiccards.core.model.aggr.AbstractPowerAggregator;
import com.reflexit.magiccards.core.model.aggr.CollisionAggregator;
import com.reflexit.magiccards.core.model.aggr.DateAggregator;
import com.reflexit.magiccards.core.model.aggr.FieldCount4Aggregator;
import com.reflexit.magiccards.core.model.aggr.FieldCreatureCountAggregator;
import com.reflexit.magiccards.core.model.aggr.FieldLegalityMapAggregator;
import com.reflexit.magiccards.core.model.aggr.FieldOwnCountAggregator;
import com.reflexit.magiccards.core.model.aggr.FieldOwnUniqueAggregator;
import com.reflexit.magiccards.core.model.aggr.FieldProggress4Aggregator;
import com.reflexit.magiccards.core.model.aggr.FieldProggressAggregator;
import com.reflexit.magiccards.core.model.aggr.FieldSizeAggregator;
import com.reflexit.magiccards.core.model.aggr.FieldUniqueAggregator;
import com.reflexit.magiccards.core.model.aggr.StringAggregator;

public enum MagicCardField implements ICardField {
	ID {
		@Override
		public ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}

		@Override
		public void setM(MagicCard card, Object value) {
			card.setCardId(castToInteger(value));
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getCardId();
		};
	},
	NAME {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setName(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getName();
		};
	},
	COST {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setCost(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getCost();
		};
	},
	TYPE {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setType(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getType();
		};
	},
	POWER {
		@Override
		public ICardVisitor getAggregator() {
			return new AbstractPowerAggregator(this);
		}

		@Override
		protected void setStr(MagicCard card, String value) {
			card.setPower(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getPower();
		};
	},
	TOUGHNESS {
		@Override
		public ICardVisitor getAggregator() {
			return new AbstractPowerAggregator(this);
		}

		@Override
		protected void setStr(MagicCard card, String value) {
			card.setToughness(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getToughness();
		};
	},
	ORACLE("oracleText") {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setOracleText(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getOracleText();
		};
	},
	SET("edition") {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setSet(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getSet();
		};
	},
	RARITY {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setRarity(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getRarity();
		};
	},
	CTYPE(null) {
		@Override
		public Object get(IMagicCard card) {
			return card.getColorType();
		};
	},
	CMC(null) {
		// @Override
		// public Object aggregateValueOf(ICard card) {
		// Colors cl = Colors.getInstance();
		// return cl.getConvertedManaCost(((IMagicCard) card).getCost());
		// }
		@Override
		public ICardVisitor getAggregator() {
			return new CollisionAggregator(this, "*");
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getCmc();
		};
	},
	DBPRICE() {
		@Override
		public ICardVisitor getAggregator() {
			return new AbstractFloatCountAggregator(this);
		}

		@Override
		public void setM(MagicCard card, Object value) {
			card.setDbPrice(castToFloat(value));
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getDbPrice();
		};
	},
	LANG {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setLanguage(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getLanguage();
		};
	},
	EDITION_ABBR(null) {
		@Override
		public Object get(IMagicCard card) {
			return card.getEdition().getMainAbbreviation();
		};
	},
	RATING {
		@Override
		public ICardVisitor getAggregator() {
			return new AbstractFloatCountAggregator(this);
		}

		@Override
		public void setM(MagicCard card, Object value) {
			card.setRating(castToFloat(value));
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getRating();
		};
	},
	ARTIST {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setArtist(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getArtist();
		};
	},
	COLLNUM("num") { // collector number value.e. 5/234
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setCollNumber(value);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getCollNumber();
		};
	},
	RULINGS {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setRulings(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getRulings();
		};
	},
	TEXT {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setText(value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getText();
		};
	},
	ENID("enId") {
		@Override
		public ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}

		@Override
		public void setM(MagicCard card, Object value) {
			card.setEnglishCardId(castToInteger(value));
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getEnglishCardId();
		};
	},
	NOUPDATE(null) {
		@Override
		public void setM(MagicCard card, Object value) {
			if (value instanceof String || value == null)
				card.setProperty(this, Boolean.valueOf((String) value));
			else if (value instanceof Boolean)
				card.setProperty(this, value);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getProperty(this);
		};
	},
	FLIPID(null) {
		@Override
		public ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}

		@Override
		public void setM(MagicCard card, Object value) {
			card.setPropertyInteger(this, value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getFlipId();
		};
	},
	COLOR_INDICATOR(null) {
		@Override
		public ICardVisitor getAggregator() {
			return new StringAggregator(this);
		}

		@Override
		protected void setStr(MagicCard card, String value) {
			card.setPropertyString(this, value);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getProperty(this);
		};
	},
	PART(null) {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setPropertyString(this, value);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getPart();
		};
	},
	OTHER_PART(null) {
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setPropertyString(this, value);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getProperty(this);
		};
	},
	SET_BLOCK(null) { // block of the set
		@Override
		public Object get(IMagicCard card) {
			return card.getEdition().getBlock();
		};
	},
	SET_CORE(null) { // type of the set (Core, Expantions, etc)
		@Override
		public Object get(IMagicCard card) {
			return card.getEdition().getType();
		};
	},
	SET_RELEASE(null) { // release date of the set
		@Override
		public ICardVisitor getAggregator() {
			return new DateAggregator(this);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getEdition().getReleaseDate();
		};
	},
	UNIQUE_COUNT(null) { // count of unique cards (usually only make sense for
							// group)
		@Override
		public ICardVisitor getAggregator() {
			return new FieldUniqueAggregator(this);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getUniqueCount();
		};
	},
	SIZE(null) { // flat size of the group, size of non-groupped element is
					// always 1
		@Override
		public ICardVisitor getAggregator() {
			return new FieldSizeAggregator(this);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getUniqueCount();
		};
	},
	SIDE(null) { // for multi sides/duble/flip card represent version of card (0
					// or 1)
		@Override
		public ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}

		@Override
		public void setM(MagicCard card, Object value) {
			card.setPropertyInteger(this, value);
		}

		@Override
		public Object get(IMagicCard card) {
			return card.getSide();
		};
	},
	IMAGE_URL(null) { // for non gatherer cards
		@Override
		protected void setStr(MagicCard card, String value) {
			card.setImageUrl(value);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getImageUrl();
		};
	},
	LEGALITY(null) {
		@Override
		public ICardVisitor getAggregator() {
			return new FieldLegalityMapAggregator(this);
		}

		@Override
		public void setM(MagicCard card, Object value) {
			card.setProperty(MagicCardField.LEGALITY, value);
		};

		@Override
		public Object get(IMagicCard card) {
			return card.getLegalityMap();
		};
	},
	COLOR(null) {
		@Override
		public Object get(IMagicCard card) {
			return card.getCost();
		};

		@Override
		public void set(IMagicCard card, Object value) {
			// ignore
		}
	},
	COLOR_IDENTITY(null) {
		@Override
		public ICardVisitor getAggregator() {
			return new StringAggregator(this);
		}

		@Override
		public Object get(IMagicCard card) {
			return Colors.getInstance().getColorIdentityAsCost(card);
		};

		@Override
		public void set(IMagicCard card, Object value) {
			// ignore
		}
	},
	ENGLISH_NAME(null) { // block of the set
		@Override
		public ICardVisitor getAggregator() {
			return new StringAggregator(this);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getEnglishName();
		}

		@Override
		public void setM(MagicCard card, Object value) {
			// ignore
		}
	},
	ENGLISH_TYPE(null) { // block of the set
		@Override
		public ICardVisitor getAggregator() {
			return new StringAggregator(this);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getEnglishType();
		}

		@Override
		public void setM(MagicCard card, Object value) {
			// ignore
		}
	},
	HASHCODE(null) {
		@Override
		public ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}

		@Override
		public Object get(IMagicCard card) {
			return System.identityHashCode(card);
		};
	},
	PROPERTIES {
		@Override
		public void setM(MagicCard card, Object value) {
			if (value instanceof String)
				card.setProperties((String) value);
			else if (value == null)
				card.setProperties((LinkedHashMap<ICardField, Object>) null);
			else if (value instanceof LinkedHashMap)
				card.setProperties((LinkedHashMap) ((LinkedHashMap) value).clone());
			else
				throw new ClassCastException();
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getProperties();
		};
	},
	// end of magic base fields
	COUNT(true) {
		@Override
		public ICardVisitor getAggregator() {
			return new AbstractIntTransAggregator(this);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getCount();
		};

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getCount();
		}

		@Override
		public void setM(MagicCardPhysical card, Object value) {
			if (value instanceof Integer)
				card.setCount((Integer) value);
			else
				card.setCount(Integer.parseInt((String) value));
		}
	},
	PRICE(true) {
		@Override
		public ICardVisitor getAggregator() {
			return new AbstractFloatCountAggregator(this);
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getPrice();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			if (value instanceof Float)
				card.setPrice((Float) value);
			else
				card.setPrice(Float.parseFloat((String) value));
		}
	},
	COMMENT(true) {
		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getComment();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			card.setComment((String) value);
		}
	},
	LOCATION(true) {
		@Override
		public ICardVisitor getAggregator() {
			return new CollisionAggregator(this, Location.NO_WHERE);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getLocation();
		};

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getLocation();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			if (value instanceof Location)
				card.setLocation((Location) value);
			else
				card.setLocation(Location.valueOf((String) value));
		}
	},
	CUSTOM(true) {
		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getCustom();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			card.setCustom((String) value);
		}
	},
	OWNERSHIP(true) {
		@Override
		public ICardVisitor getAggregator() {
			return new CollisionAggregator(this, Boolean.TRUE);
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.isOwn();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			if (value instanceof Boolean)
				card.setOwn((Boolean) value);
			else
				card.setOwn(Boolean.parseBoolean((String) value));
		}
	},
	FORTRADECOUNT("forTrade", true) {
		@Override
		public ICardVisitor getAggregator() {
			return new AbstractIntTransAggregator(this);
		}

		@Override
		public boolean isTransient() {
			return true;
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getForTrade();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			if (value instanceof Integer)
				card.setProperty(MagicCardField.FORTRADECOUNT, value);
			else
				card.setProperty(MagicCardField.FORTRADECOUNT, Integer.parseInt((String) value));
		}
	},
	SPECIAL(true) { // like foil, premium, mint, played, online etc
		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getSpecial();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			card.setSpecial((String) value);
		}
	},
	SIDEBOARD(null, true) {
		@Override
		public Object aggregateValueOf(ICard card) {
			return ((IMagicCardPhysical) card).isSideboard();
		}

		@Override
		public Object getM(MagicCard card) {
			return card.isSideboard();
		};

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.isSideboard();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			MagicLogger.log(new Exception("Attempt to set sideboad field"));
		}
	},
	OWN_COUNT(null, true) {
		@Override
		public ICardVisitor getAggregator() {
			return new FieldOwnCountAggregator(this);
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getOwnCount();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			// ignore
		}
	}, // count of own card (normal count counts own and virtual)
	OWN_UNIQUE(null, true) {
		@Override
		public ICardVisitor getAggregator() {
			return new FieldOwnUniqueAggregator(this);
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getUniqueCount();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			// ignore
		}
	}, // count of own unique cards (only applies to groups usually)
	CREATURE_COUNT(null, true) {
		@Override
		public ICardVisitor getAggregator() {
			return new FieldCreatureCountAggregator(this);
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getCreatureCount();
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getCreatureCount();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			// ignore
		}
	},
	COUNT4(null, true) {
		@Override
		public ICardVisitor getAggregator() {
			return new FieldCount4Aggregator(this);
		}

		@Override
		public Object getM(MagicCard card) {
			return card.getCount4();
		};

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getCount4();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			// ignore
		}
	},
	PERCENT_COMPLETE(null, true) {
		@Override
		public ICardVisitor getAggregator() {
			return new FieldProggressAggregator(this);
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			int c = card.getOwnCount();
			if (c > 0)
				return 100f;
			else
				return 0f;
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			// ignore
		}
	},
	PERCENT4_COMPLETE(null, true) {
		@Override
		public ICardVisitor getAggregator() {
			return new FieldProggress4Aggregator(this);
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			int c = card.getCount4();
			return (float) c * 100 / 4;
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			// ignore
		}
	},
	DATE(true) { // creation date of the card instance
		@Override
		public ICardVisitor getAggregator() {
			return new DateAggregator(this);
		}

		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getDate();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			if (value instanceof String) {
				card.setDate((String) value);
			} else {
				card.setDate((Date) value);
			}
		}
	},
	ERROR(null, true) {// error field for import
		@Override
		public Object getM(MagicCardPhysical card) {
			return card.getError();
		}

		@Override
		protected void setM(MagicCardPhysical card, Object value) {
			card.setError(value);
		}
	},
	// end of fields
	;
	private final String tag;
	private final String property;
	private final boolean phys;
	private ICardVisitor aggregator;

	MagicCardField() {
		this(false);
	}

	MagicCardField(String javaField) {
		this(javaField, false);
	}

	MagicCardField(boolean physical) {
		property = name().toLowerCase(Locale.ENGLISH);
		tag = property;
		phys = physical;
		aggregator = getAggregator();
	}

	MagicCardField(String javaField, boolean physical) {
		property = name().toLowerCase(Locale.ENGLISH);
		tag = javaField;
		phys = physical;
		aggregator = getAggregator();
	}

	public ICardVisitor getAggregator() {
		return new StringAggregator(this);
	}

	@Override
	public boolean isTransient() {
		return tag == null;
	}

	public static ICardField[] allFields() {
		MagicCardField[] values = MagicCardField.values();
		return values;
	}

	public static ICardField[] allNonTransientFields(boolean phys) {
		MagicCardField[] values = MagicCardField.values();
		ArrayList<ICardField> res = new ArrayList<ICardField>();
		for (MagicCardField f : values) {
			if (!f.isTransient()) {
				if (phys || !f.phys)
					res.add(f);
			}
		}
		return res.toArray(new ICardField[res.size()]);
	}

	/**
	 * How this field is written to external source, such as xml
	 */
	public String getTag() {
		return tag;
	}

	public String getProperty() {
		return property;
	}

	/**
	 * If field represents a special tag, what is the tag name
	 *
	 * @return
	 */
	String specialTag() {
		if (getTag() == null)
			return null;
		return getTag().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public Object aggregateValueOf(ICard card) {
		return card.accept(aggregator, null);
	}

	public static ICardField fieldByName(String field) {
		if (field == null || field.length() == 0)
			return null;
		try {
			return valueOf(field);
		} catch (Exception e) {
			// ignore
		}
		// aliases
		if (field.equals("EDITION"))
			return SET;
		if (field.equals("QTY"))
			return COUNT;
		// // legacy
		// if (field.equals("CUSTOM"))
		// return LegacyField.INSTANCE;
		return null;
	}

	public static ICardField[] toFields(String line, String sep) {
		String split[] = line.split(sep);
		ICardField res[] = new ICardField[split.length];
		for (int i = 0; i < split.length; i++) {
			String string = split[i];
			ICardField field = fieldByName(string);
			res[i] = field;
		}
		return res;
	}

	@Override
	public String getLabel() {
		String name = name();
		name = name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
		name = name.replace('_', ' ');
		return name;
	}

	public boolean isPhysical() {
		return phys;
	}

	protected void setStr(MagicCard card, String value) {
		throw new IllegalArgumentException("Not settable " + this);
	}

	public void setM(MagicCard card, Object value) {
		if (value instanceof String || value == null) {
			setStr(card, (String) value);
		} else
			throw new IllegalArgumentException("Not supported type " + value.getClass() + " for " + this);
	}

	protected void setM(MagicCardPhysical card, Object value) {
		if (!isPhysical())
			setM(card.getBase(), value);
		else
			throw new IllegalArgumentException("Not settable " + this);
	}

	public void set(IMagicCard card, Object value) {
		if (card instanceof MagicCard)
			setM((MagicCard) card, value);
		else if (card instanceof MagicCardPhysical)
			setM((MagicCardPhysical) card, value);
		else
			throw new IllegalArgumentException("Don't know this class " + card.getClass());
	}

	public static Float castToFloat(Object value) {
		if (value instanceof String)
			return Float.parseFloat((String) value);
		if (value instanceof Float)
			return (Float) value;
		if (value == null)
			return null;
		throw new ClassCastException(value.getClass().toString());
	}

	public static Integer castToInteger(Object value) {
		if (value instanceof String)
			return Integer.parseInt((String) value);
		if (value instanceof Integer)
			return (Integer) value;
		if (value == null)
			return null;
		throw new ClassCastException();
	}

	public Object get(IMagicCard card) {
		if (card instanceof MagicCard) {
			return getM((MagicCard) card);
		}
		if (card instanceof MagicCardPhysical) {
			return getM((MagicCardPhysical) card);
		}
		throw new IllegalArgumentException("Don't know this class " + card.getClass());
	}

	protected Object getM(MagicCard card) {
		if (isPhysical()) {
			CardGroup realCards = card.getRealCards();
			if (realCards != null) {
				return realCards.get(this);
			} else {
				return null;
			}
		}
		throw new IllegalArgumentException("Not implemented");
	}

	protected Object getM(MagicCardPhysical card) {
		if (isPhysical())
			throw new IllegalArgumentException("Not implemented");
		return getM(card.getBase());
	}
}
