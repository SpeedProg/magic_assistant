package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Locale;

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
import com.reflexit.magiccards.core.model.aggr.FieldUniqueAggregator;
import com.reflexit.magiccards.core.model.aggr.StringAggregator;

public enum MagicCardField implements ICardField {
	ID {
		@Override
		protected ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}
	},
	NAME,
	COST,
	TYPE,
	POWER {
		@Override
		protected ICardVisitor getAggregator() {
			return new AbstractPowerAggregator(this);
		}
	},
	TOUGHNESS {
		@Override
		protected ICardVisitor getAggregator() {
			return new AbstractPowerAggregator(this);
		}
	},
	ORACLE("oracleText"),
	SET("edition"),
	RARITY,
	CTYPE(null) {
		@Override
		public Object aggregateValueOf(ICard card) {
			Colors cl = Colors.getInstance();
			return cl.getColorType(card.getString(MagicCardField.COST));
		}
	},
	CMC(null) {
		@Override
		public Object aggregateValueOf(ICard card) {
			Colors cl = Colors.getInstance();
			return cl.getConvertedManaCost(card.getString(MagicCardField.COST));
		}
	},
	DBPRICE() {
		@Override
		protected ICardVisitor getAggregator() {
			return new AbstractFloatCountAggregator(this);
		}
	},
	LANG,
	EDITION_ABBR(null),
	RATING {
		@Override
		protected ICardVisitor getAggregator() {
			return new AbstractFloatCountAggregator(this);
		}
	},
	ARTIST,
	COLLNUM("num"), // collector number value.e. 5/234
	RULINGS,
	TEXT,
	ENID("enId") {
		@Override
		protected ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}
	},
	PROPERTIES,
	FLIPID(null) {
		@Override
		protected ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}
	},
	PART(null),
	OTHER_PART(null),
	SET_BLOCK(null), // block of the set
	SET_CORE(null), // type of the set (Core, Expantions, etc)
	SET_RELEASE(null) { // release date of the set
		@Override
		protected ICardVisitor getAggregator() {
			return new DateAggregator(this);
		}
	},
	UNIQUE_COUNT(null) {
		@Override
		public Object aggregateValueOf(ICard card) {
			return card.accept(FieldUniqueAggregator.getInstance(), null);
		}
	}, // count of unique cards (usually only make sense for group)
	SIDE(null) {
		@Override
		protected ICardVisitor getAggregator() {
			return new CollisionAggregator(this, 0);
		}
	}, // for multi sides/duble/flip card represent version of card (0 or 1)
	IMAGE_URL(null), // for non gatherer cards
	LEGALITY(null) {
		@Override
		protected ICardVisitor getAggregator() {
			return new FieldLegalityMapAggregator(this);
		}
	},
	COLOR(null),
	// end of magic base fields
	COUNT(true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new AbstractIntTransAggregator(this);
		}
	},
	PRICE(true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new AbstractFloatCountAggregator(this);
		}
	},
	COMMENT(true),
	LOCATION(true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new CollisionAggregator(this, Location.NO_WHERE);
		}
	},
	CUSTOM(true),
	OWNERSHIP(true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new CollisionAggregator(this, Boolean.TRUE);
		}
	},
	FORTRADECOUNT("forTrade", true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new AbstractIntTransAggregator(this);
		}
	},
	SPECIAL(true), // like foil, premium, mint, played, online etc
	SIDEBOARD(null, true) {
		@Override
		public Object aggregateValueOf(ICard card) {
			return ((IMagicCardPhysical) card).isSideboard();
		}
	},
	OWN_COUNT(null, true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new FieldOwnCountAggregator(this);
		}
	}, // count of own card (normal count counts own and virtual)
	OWN_UNIQUE(null, true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new FieldOwnUniqueAggregator(this);
		}
	}, // count of own unique cards (only applies to groups usually)
	CREATURE_COUNT(null, true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new FieldCreatureCountAggregator(this);
		}
	},
	COUNT4(null, true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new FieldCount4Aggregator(this);
		}
	},
	PERCENT_COMPLETE(null, true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new FieldProggressAggregator(this);
		}
	},
	PERCENT4_COMPLETE(null, true) {
		@Override
		protected ICardVisitor getAggregator() {
			return new FieldProggress4Aggregator(this);
		}
	},
	DATE(true) { // release date of the set
		@Override
		protected ICardVisitor getAggregator() {
			return new DateAggregator(this);
		}
	},
	ERROR(null, true), // error field for import
	// end of fields
	;
	private final String tag;
	private final boolean phys;
	private ICardVisitor aggregator;

	MagicCardField() {
		this(false);
	}

	MagicCardField(String javaField) {
		this(javaField, false);
	}

	MagicCardField(boolean physical) {
		tag = name().toLowerCase(Locale.ENGLISH);
		phys = physical;
		aggregator = getAggregator();
	}

	MagicCardField(String javaField, boolean physical) {
		tag = javaField;
		phys = physical;
		aggregator = getAggregator();
	}

	protected ICardVisitor getAggregator() {
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

	@Override
	public String getTag() {
		return tag;
	}

	@Override
	public Object aggregateValueOf(ICard card) {
		return card.accept(aggregator, null);
	}

	public static ICardField fieldByName(String field) {
		if (field == null || field.length() == 0)
			return null;
		try {
			MagicCardField f = valueOf(field);
			if (f != null)
				return f;
		} catch (Exception e) {
			// ignore
		}
		// aliases
		if (field.equals("EDITION"))
			return SET;
		if (field.equals("QTY"))
			return COUNT;
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
}
