package com.reflexit.magiccards.core.locale;

import org.eclipse.osgi.util.NLS;

public final class CardText extends NLS {
	private static final String BUNDLE_NAME = CardText.class.getName();
	public static String Type_Artifact;
	public static String Type_Basic;
	public static String Type_Creature;
	public static String Type_Enchantment;
	public static String Type_Instant;
	public static String Type_Interrupt;
	public static String Type_Land;
	public static String Type_Non_Creature;
	public static String Type_Planeswalker;
	public static String Type_Sorcery;
	public static String Type_Spell;
	public static String Type_Summon;
	public static String Type_Unknown;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CardText.class);
	}

	private CardText() {
	}
}
