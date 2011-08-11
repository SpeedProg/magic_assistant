package com.reflexit.magiccards.core.locale;

import java.util.HashMap;
import java.util.Locale;

public class CardTextLocal extends LocalizedText {
	private static final String BUNDLE_NAME = CardText.class.getName();
	public String Type_Artifact;
	public String Type_Basic;
	public String Type_Creature;
	public String Type_Enchantment;
	public String Type_Instant;
	public String Type_Interrupt;
	public String Type_Land;
	public String Type_Non_Creature;
	public String Type_Planeswalker;
	public String Type_Sorcery;
	public String Type_Spell;
	public String Type_Summon;
	public String Type_Unknown;
	protected static HashMap<Locale, CardTextLocal> map = new HashMap<Locale, CardTextLocal>();

	public static CardTextLocal getCardText(Locale locale) {
		CardTextLocal ct = map.get(locale);
		if (ct == null) {
			ct = new CardTextLocal(locale);
			map.put(locale, ct);
		}
		return ct;
	}

	private CardTextLocal(Locale locale) {
		super(locale);
	}

	@Override
	public String getBundleName() {
		return BUNDLE_NAME;
	}

	public static CardTextLocal getCardText(String language) {
		Locale loc = getLocale(language);
		return getCardText(loc);
	}
}
