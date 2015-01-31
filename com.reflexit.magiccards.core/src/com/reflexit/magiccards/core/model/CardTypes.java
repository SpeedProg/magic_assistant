package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.locale.CardTextLocal;
import com.reflexit.magiccards.core.locale.LocalizedText;

public class CardTypes implements ISearchableProperty {
	public static CardTextLocal TYPES = CardTextLocal.getCardText(LocalizedText.ENGLISH);

	private CardTypes() {
		this.names = new LinkedHashMap<String, String>();
		add(TYPES.Type_Land);
		add(TYPES.Type_Creature);
		add(TYPES.Type_Instant);
		add(TYPES.Type_Sorcery);
		add(TYPES.Type_Enchantment);
		add(TYPES.Type_Artifact);
		add(TYPES.Type_Planeswalker);
	}

	static CardTypes instance = new CardTypes();
	private LinkedHashMap<String, String> names;

	private void add(String string) {
		String id = getPrefConstant(string);
		this.names.put(id, string);
	}

	public boolean hasType(IMagicCard card, String type) {
		String typeText = card.getType();
		if (containsType(typeText, type))
			return true;
		String language = card.getLanguage();
		if (language != null && language.length() > 0) {
			CardTextLocal localized = CardTextLocal.getCardText(language);
			String localizedType = TYPES.translate(type, localized);
			if (localizedType != null && containsType(typeText, localizedType))
				return true;
		}
		if (type == TYPES.Type_Creature) {
			return hasType(card, TYPES.Type_Summon);
		}
		if (type == TYPES.Type_Instant) {
			return hasType(card, TYPES.Type_Interrupt);
		}
		return false;
	}

	private boolean containsType(String text, String type) {
		if (text == null)
			return false;
		return Pattern.compile(type, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(text).find();
	}

	@Override
	public String getIdPrefix() {
		return getFilterField().toString();
	}

	@Override
	public FilterField getFilterField() {
		return FilterField.CARD_TYPE;
	}

	public static CardTypes getInstance() {
		return instance;
	}

	@Override
	public Collection<String> getIds() {
		return new ArrayList<String>(this.names.keySet());
	}

	public String getPrefConstant(String name) {
		return FilterField.getPrefConstant(getIdPrefix(), name);
	}

	@Override
	public String getNameById(String id) {
		return this.names.get(id);
	}

	public String getLocalizedNameById(String id) {
		String enName = getNameById(id);
		return TYPES.translate(enName, Locale.getDefault());
	}

	public static String[] proposals = new String[] {//
	"Basic", "Tribal", "World", "Legendary", "Snow",
			//
			"Land", "Artifact", "Creature",
			//
			"Elf", "Goblin", "Human", "Elemental", "Kithkin" };

	/**
	 * @return
	 */
	public static String[] getProposals() {
		return proposals;
	}

	public Collection<String> getLocalizedNames() {
		ArrayList<String> names2 = new ArrayList<String>();
		for (Iterator<String> iterator = names.values().iterator(); iterator.hasNext();) {
			String string = iterator.next();
			names2.add(TYPES.translate(string, Locale.getDefault()));
		}
		return names2;
	}
}
