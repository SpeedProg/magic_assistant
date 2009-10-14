package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.Activator;

public class FilterHelper {
	private static final String PREFIX = Activator.PLUGIN_ID;
	public static final String SUBTYPE = "subtype";
	public static final String TYPE_LINE = SUBTYPE;
	public static final String TEXT_LINE = "oracleText";
	public static final String NAME_LINE = "name";
	public static final String TEXT_POSTFIX = "text";
	public static final String POWER = "power";
	public static final String NUMERIC_POSTFIX = "numeric";
	public static final String TOUGHNESS = "toughness";
	public static final String CCC = "convertedCC";
	public static final String EDITION = "edition";
	public static final String LOCATION = "location";
	public static final String RARITY = "rarity";
	public static final String GROUP_FIELD = "group_field";

	public static String toIdent(String string) {
		String res = string.toLowerCase();
		res = res.replaceAll("^\\W", "_");
		return res;
	}

	public static String getPrefConstant(String sub, String name) {
		return PREFIX + ".filter." + sub + "." + toIdent(name);
	}

	public static String getTypePrefConstant(String name) {
		return getPrefConstant("type", name);
	}

	public static String getSuperTypePrefConstant(String name) {
		return getPrefConstant("supertype", name);
	}

	public static Collection getAllIds() {
		ArrayList ids = new ArrayList();
		ids.addAll(Colors.getInstance().getIds());
		ids.addAll(ColorTypes.getInstance().getIds());
		ids.addAll(CardTypes.getInstance().getIds());
		ids.addAll(SuperTypes.getInstance().getIds());
		ids.addAll(Editions.getInstance().getIds());
		ids.addAll(Rarity.getInstance().getIds());
		ids.addAll(Locations.getInstance().getIds());
		ids.add(FilterHelper.getPrefConstant(FilterHelper.SUBTYPE, FilterHelper.TEXT_POSTFIX));
		ids.add(FilterHelper.getPrefConstant(FilterHelper.TEXT_LINE, FilterHelper.TEXT_POSTFIX));
		ids.add(FilterHelper.getPrefConstant(FilterHelper.NAME_LINE, FilterHelper.TEXT_POSTFIX));
		ids.add(FilterHelper.getPrefConstant(FilterHelper.POWER, FilterHelper.NUMERIC_POSTFIX));
		ids.add(FilterHelper.getPrefConstant(FilterHelper.TOUGHNESS, FilterHelper.NUMERIC_POSTFIX));
		ids.add(FilterHelper.getPrefConstant(FilterHelper.CCC, FilterHelper.NUMERIC_POSTFIX));
		// TODO add the rest
		return ids;
	}
}
