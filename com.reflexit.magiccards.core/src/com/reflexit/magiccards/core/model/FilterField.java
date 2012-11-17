package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.DataManager;

public enum FilterField {
	COLOR("colors"),
	CARD_TYPE("types"),
	GROUP_FIELD("group_field"),
	TYPE_LINE(MagicCardField.TYPE),
	TEXT_LINE(MagicCardField.TEXT),
	NAME_LINE(MagicCardField.NAME),
	POWER(MagicCardField.POWER),
	TOUGHNESS(MagicCardField.TOUGHNESS),
	CCC(MagicCardField.CMC),
	EDITION(MagicCardField.SET),
	RARITY(MagicCardField.RARITY),
	LOCATION(MagicCardFieldPhysical.LOCATION),
	PRICE(MagicCardFieldPhysical.PRICE),
	DBPRICE(MagicCardField.DBPRICE),
	COMMUNITYRATING(MagicCardField.RATING),
	ARTIST(MagicCardField.ARTIST),
	COUNT(MagicCardFieldPhysical.COUNT),
	COMMENT(MagicCardFieldPhysical.COMMENT),
	OWNERSHIP(MagicCardFieldPhysical.OWNERSHIP),
	LANG(MagicCardField.LANG),
	TEXT_LINE_2(TEXT_LINE + "_2"),
	TEXT_LINE_3(TEXT_LINE + "_3"),
	TEXT_NOT_1(TEXT_LINE + "_exclude_1"),
	TEXT_NOT_2(TEXT_LINE + "_exclude_2"),
	TEXT_NOT_3(TEXT_LINE + "_exclude_3"),
	COLLNUM(MagicCardField.COLLNUM),
	SPECIAL(MagicCardFieldPhysical.SPECIAL),
	FORTRADECOUNT(MagicCardFieldPhysical.FORTRADECOUNT);
	private String id;
	private static final String PREFIX = DataManager.ID;
	public static final String TEXT_POSTFIX = "text";
	public static final String NUMERIC_POSTFIX = "numeric";

	FilterField(ICardField field) {
		id = field.name();
	}

	FilterField(String s) {
		id = s;
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
		ids.addAll(SuperTypes.getInstance().getIds());
		ids.addAll(Editions.getInstance().getIds());
		ids.addAll(Rarity.getInstance().getIds());
		ids.addAll(Locations.getInstance().getIds());
		ids.add(getPrefConstant(TEXT_LINE, TEXT_POSTFIX));
		ids.add(getPrefConstant(TYPE_LINE, TEXT_POSTFIX));
		ids.add(getPrefConstant(NAME_LINE, TEXT_POSTFIX));
		ids.add(getPrefConstant(POWER, NUMERIC_POSTFIX));
		ids.add(getPrefConstant(TOUGHNESS, NUMERIC_POSTFIX));
		ids.add(getPrefConstant(CCC, NUMERIC_POSTFIX));
		ids.add(getPrefConstant(COUNT, NUMERIC_POSTFIX));
		ids.add(getPrefConstant(PRICE, NUMERIC_POSTFIX));
		ids.add(getPrefConstant(DBPRICE, NUMERIC_POSTFIX));
		ids.add(getPrefConstant(COMMUNITYRATING, NUMERIC_POSTFIX));
		ids.add(getPrefConstant(COLLNUM, NUMERIC_POSTFIX));
		ids.add(getPrefConstant(ARTIST, TEXT_POSTFIX));
		ids.add(getPrefConstant(COMMENT, TEXT_POSTFIX));
		ids.add(getPrefConstant(OWNERSHIP, TEXT_POSTFIX));
		ids.add(getPrefConstant(TEXT_LINE_2, TEXT_POSTFIX));
		ids.add(getPrefConstant(TEXT_LINE_3, TEXT_POSTFIX));
		ids.add(getPrefConstant(TEXT_NOT_1, TEXT_POSTFIX));
		ids.add(getPrefConstant(TEXT_NOT_2, TEXT_POSTFIX));
		ids.add(getPrefConstant(TEXT_NOT_3, TEXT_POSTFIX));
		ids.add(getPrefConstant(MagicCardFieldPhysical.FORTRADECOUNT.name(), FilterField.NUMERIC_POSTFIX));
		ids.add(getPrefConstant(MagicCardFieldPhysical.SPECIAL.name(), FilterField.TEXT_POSTFIX));
		ids.add(getPrefConstant(FilterField.LANG, FilterField.TEXT_POSTFIX));
		// TODO add the rest
		return ids;
	}
}
