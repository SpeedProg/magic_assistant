package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.DataManager;

public enum FilterField {
	COLOR(MagicCardField.COST, "colors", Postfix.ENUM_POSTFIX),
	CARD_TYPE(MagicCardField.TYPE, "types", Postfix.ENUM_POSTFIX),
	GROUP_FIELD(null, "group_field", Postfix.TEXT_POSTFIX),
	TYPE_LINE(MagicCardField.TYPE, Postfix.TEXT_POSTFIX),
	TEXT_LINE(MagicCardField.TEXT, Postfix.TEXT_POSTFIX),
	NAME_LINE(MagicCardField.NAME, Postfix.TEXT_POSTFIX),
	POWER(MagicCardField.POWER, Postfix.NUMERIC_POSTFIX),
	TOUGHNESS(MagicCardField.TOUGHNESS, Postfix.NUMERIC_POSTFIX),
	CCC(MagicCardField.CMC, Postfix.NUMERIC_POSTFIX),
	EDITION(MagicCardField.SET, Postfix.ENUM_POSTFIX),
	RARITY(MagicCardField.RARITY, Postfix.ENUM_POSTFIX),
	LOCATION(MagicCardFieldPhysical.LOCATION, Postfix.ENUM_POSTFIX),
	PRICE(MagicCardFieldPhysical.PRICE, Postfix.NUMERIC_POSTFIX),
	DBPRICE(MagicCardField.DBPRICE, Postfix.NUMERIC_POSTFIX),
	COMMUNITYRATING(MagicCardField.RATING, Postfix.NUMERIC_POSTFIX),
	ARTIST(MagicCardField.ARTIST, Postfix.TEXT_POSTFIX),
	COUNT(MagicCardFieldPhysical.COUNT, Postfix.NUMERIC_POSTFIX),
	COMMENT(MagicCardFieldPhysical.COMMENT, Postfix.TEXT_POSTFIX),
	OWNERSHIP(MagicCardFieldPhysical.OWNERSHIP, Postfix.TEXT_POSTFIX),
	LANG(MagicCardField.LANG, Postfix.TEXT_POSTFIX),
	TEXT_LINE_2(MagicCardField.ORACLE, TEXT_LINE + "_2", Postfix.TEXT_POSTFIX),
	TEXT_LINE_3(MagicCardField.ORACLE, TEXT_LINE + "_3", Postfix.TEXT_POSTFIX),
	TEXT_NOT_1(MagicCardField.ORACLE, TEXT_LINE + "_exclude_1", Postfix.TEXT_POSTFIX),
	TEXT_NOT_2(MagicCardField.ORACLE, TEXT_LINE + "_exclude_2", Postfix.TEXT_POSTFIX),
	TEXT_NOT_3(MagicCardField.ORACLE, TEXT_LINE + "_exclude_3", Postfix.TEXT_POSTFIX),
	COLLNUM(MagicCardField.COLLNUM, Postfix.NUMERIC_POSTFIX),
	SPECIAL(MagicCardFieldPhysical.SPECIAL, Postfix.TEXT_POSTFIX),
	FORTRADECOUNT(MagicCardFieldPhysical.FORTRADECOUNT, Postfix.NUMERIC_POSTFIX),
	FORMAT(MagicCardField.LEGALITY, Postfix.TEXT_POSTFIX);
	// fields
	private ICardField field;
	private String id;
	private String postfix;
	private static final String PREFIX = DataManager.ID;

	static class Postfix {
		public static final String TEXT_POSTFIX = "text";
		public static final String NUMERIC_POSTFIX = "numeric";
		public static final String ENUM_POSTFIX = "";
	}

	private FilterField(ICardField field, String postfix) {
		this(field, field.name(), postfix);
	}

	private FilterField(ICardField field, String s, String postfix) {
		this.field = field;
		this.id = s;
		this.postfix = postfix;
	}

	public String getPrefConstant() {
		return PREFIX + ".filter." + toString() + "." + postfix;
	}

	@Override
	public String toString() {
		return id;
	}

	public static String escapeProperty(String string) {
		String res = string.toLowerCase();
		res = res.replaceAll("[^\\w-./]", "_");
		return res;
	}

	public static String getPrefConstant(String sub, String name) {
		return PREFIX + ".filter." + sub + "." + escapeProperty(name);
	}

	public static String getPrefConstant(FilterField sub, String name) {
		return PREFIX + ".filter." + sub.toString() + "." + escapeProperty(name);
	}

	public static Collection getAllIds() {
		ArrayList<String> ids = new ArrayList<String>();
		ids.addAll(Colors.getInstance().getIds());
		ids.addAll(ColorTypes.getInstance().getIds());
		ids.addAll(CardTypes.getInstance().getIds());
		ids.addAll(Editions.getInstance().getIds());
		ids.addAll(Rarity.getInstance().getIds());
		ids.addAll(Locations.getInstance().getIds());
		ids.add(TEXT_LINE.getPrefConstant());
		ids.add(TYPE_LINE.getPrefConstant());
		ids.add(NAME_LINE.getPrefConstant());
		ids.add(POWER.getPrefConstant());
		ids.add(TOUGHNESS.getPrefConstant());
		ids.add(CCC.getPrefConstant());
		ids.add(COUNT.getPrefConstant());
		ids.add(PRICE.getPrefConstant());
		ids.add(DBPRICE.getPrefConstant());
		ids.add(COMMUNITYRATING.getPrefConstant());
		ids.add(COLLNUM.getPrefConstant());
		ids.add(ARTIST.getPrefConstant());
		ids.add(COMMENT.getPrefConstant());
		ids.add(OWNERSHIP.getPrefConstant());
		ids.add(TEXT_LINE_2.getPrefConstant());
		ids.add(TEXT_LINE_3.getPrefConstant());
		ids.add(TEXT_NOT_1.getPrefConstant());
		ids.add(TEXT_NOT_2.getPrefConstant());
		ids.add(TEXT_NOT_3.getPrefConstant());
		ids.add(FORTRADECOUNT.getPrefConstant());
		ids.add(SPECIAL.getPrefConstant());
		ids.add(LANG.getPrefConstant());
		ids.add(FORMAT.getPrefConstant());
		// TODO add the rest
		return ids;
	}

	public String getPostfix() {
		return postfix;
	}

	public ICardField getField() {
		return field;
	}
}
