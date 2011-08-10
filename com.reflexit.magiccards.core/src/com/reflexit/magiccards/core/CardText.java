package com.reflexit.magiccards.core;

import org.eclipse.osgi.util.NLS;

public class CardText extends NLS {
	private static final String BUNDLE_NAME = CardText.class.getName();
	public static String CardTypes_Artifact;
	public static String CardTypes_Basic;
	public static String CardTypes_Creature;
	public static String CardTypes_Enchantment;
	public static String CardTypes_Instant;
	public static String CardTypes_Interrupt;
	public static String CardTypes_Land;
	public static String CardTypes_Non_Creature;
	public static String CardTypes_Planeswalker;
	public static String CardTypes_Sorcery;
	public static String CardTypes_Spell;
	public static String CardTypes_Summon;
	public static String CardTypes_Unknown;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CardText.class);
	}

	private CardText() {
	}
}
