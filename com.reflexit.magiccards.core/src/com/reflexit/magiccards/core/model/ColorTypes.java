package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class ColorTypes implements ISearchableProperty {
	static ColorTypes instance = new ColorTypes();
	private LinkedHashMap names;
	static public String AND_ID = getInstance().getPrefConstant("And");
	static public String ONLY_ID = getInstance().getPrefConstant("Only");

	private ColorTypes() {
		this.names = new LinkedHashMap();
		add("Multi-Color");
		add("Mono-Color");
		add("Hybrid");
		add("And");
		add("Only");
	}

	private void add(String string) {
		String id = getPrefConstant(string);
		this.names.put(id, string);
	}

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

	public Collection getNames() {
		return new ArrayList(this.names.values());
	}

	public Collection getIds() {
		return new ArrayList(this.names.keySet());
	}

	public String getPrefConstant(String name) {
		return FilterField.getPrefConstant(getIdPrefix(), name);
	}

	public String getNameById(String id) {
		return (String) this.names.get(id);
	}
}