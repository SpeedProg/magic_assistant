package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class Languages implements ISearchableProperty {
	public static enum Language {
		ENGLISH,
		RUSSIAN,
		FRENCH,
		SPANISH,
		GERMAN,
		ITALIAN,
		PORTUGESE,
		JAPANESE,
		CHINESE("Chinese Standard");
		private String lang;

		Language() {
			this.lang = name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
		}

		Language(String name) {
			this.lang = name;
		}

		public String getLang() {
			return lang;
		}
	}

	private LinkedHashMap names;

	public String[] getLangValues() {
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

	public String getIdPrefix() {
		return FilterHelper.LANG;
	}

	public Collection getNames() {
		return new ArrayList(this.names.values());
	}

	public Collection getIds() {
		return new ArrayList(this.names.keySet());
	}

	public String getPrefConstant(String name) {
		return FilterHelper.getPrefConstant(getIdPrefix(), name);
	}

	public String getNameById(String id) {
		return (String) this.names.get(id);
	}
}
