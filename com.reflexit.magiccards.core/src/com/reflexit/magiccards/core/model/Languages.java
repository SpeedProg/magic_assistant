package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;

import com.reflexit.magiccards.core.locale.LocalizedText;

public class Languages implements ISearchableProperty {
	public static enum Language {
		ENGLISH(LocalizedText.ENGLISH),
		RUSSIAN(LocalizedText.RUSSIAN),
		FRENCH(LocalizedText.FRENCH),
		SPANISH(LocalizedText.SPANISH),
		GERMAN(LocalizedText.GERMAN),
		ITALIAN(LocalizedText.ITALIAN),
		PORTUGUESE(LocalizedText.PORTUGUESE),
		JAPANESE(LocalizedText.JAPANESE),
		CHINESE_SIMPLIFIED("Chinese Simplified", LocalizedText.CHINESE_SIMPLIFIED),
		CHINESE_TRADITIONAL("Chinese Traditional", LocalizedText.CHINESE_TRADITIONAL),
		KOREAN(LocalizedText.KOREAN);
		private String lang;
		private Locale locale;

		Language(Locale locale) {
			this.lang = name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
			this.locale = locale;
		}

		Language(String name, Locale locale) {
			this.lang = name;
			this.locale = locale;
		}

		public String getLang() {
			return lang;
		}

		public Locale getLocale() {
			return locale;
		}

		public static Language fromName(String s) {
			Language[] values = Language.values();
			for (int i = 0; i < values.length; i++) {
				Language language = values[i];
				if (language.getLang().equals(s))
					return language;
			}
			if (s.equals("Chinese Standard"))
				return CHINESE_SIMPLIFIED;
			return null;
		}

		public static Language fromLocale(String s) {
			Language[] values = Language.values();
			for (int i = 0; i < values.length; i++) {
				Language language = values[i];
				if (language.getLocale().getLanguage().equals(s))
					return language;
			}
			return null;
		}
	}

	private LinkedHashMap names;

	public static String[] getLangValues() {
		Language[] values = Language.values();
		String[] res = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			Language language = values[i];
			res[i] = language.getLang();
		}
		return res;
	}

	private static Languages instance = new Languages();

	private Languages() {
		this.names = new LinkedHashMap();
		Language[] langs = Language.values();
		for (int i = 0; i < langs.length; i++) {
			Language lang = langs[i];
			add(lang.getLang());
		}
	}

	private void add(String string) {
		String id = getPrefConstant(string);
		this.names.put(id, string);
	}

	public static Languages getInstance() {
		return instance;
	}

	@Override
	public String getIdPrefix() {
		return getFilterField().toString();
	}

	@Override
	public FilterField getFilterField() {
		return FilterField.LANG;
	}

	public Collection getNames() {
		return new ArrayList(this.names.values());
	}

	@Override
	public Collection getIds() {
		return new ArrayList(this.names.keySet());
	}

	public String getPrefConstant(String name) {
		return FilterField.getPrefConstant(getIdPrefix(), name);
	}

	@Override
	public String getNameById(String id) {
		return (String) this.names.get(id);
	}

	public static Locale getLocale(String language) {
		Language[] array = Language.values();
		for (int i = 0; i < array.length; i++) {
			Language l = array[i];
			if (l.getLang().equals(language))
				return l.locale;
		}
		if (language.equals("*"))
			return Locale.ENGLISH;
		throw new IllegalArgumentException("Language Not Found: " + language);
	}
}
