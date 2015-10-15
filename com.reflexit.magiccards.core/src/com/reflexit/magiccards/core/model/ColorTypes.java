package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class ColorTypes implements ISearchableProperty {
	static ColorTypes instance = new ColorTypes();
	private LinkedHashMap<String, String> names;
	static public final String AND_ID = getInstance().getPrefConstant("And");
	static public final String ONLY_ID = getInstance().getPrefConstant("Only");
	static public final String IDENTITY_ID = getInstance().getPrefConstant("Identity");

	private ColorTypes() {
		this.names = new LinkedHashMap<String, String>();
		add("Multi-Color");
		add("Mono-Color");
		add("Hybrid");
		add("And");
		add("Only");
		add("Identity");
	}

	private void add(String string) {
		String id = getPrefConstant(string);
		this.names.put(id, string);
	}

	@Override
	public String getIdPrefix() {
		return getFilterField().toString();
	}

	@Override
	public FilterField getFilterField() {
		return FilterField.COLOR;
	}

	public static ColorTypes getInstance() {
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
}
