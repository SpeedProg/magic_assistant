package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class SuperTypes implements ISearchableProperty {
	private SuperTypes() {
		this.names = new LinkedHashMap();
		add("Artifact");
		add("Basic");
		add("Tribal");
		add("Legendary");
		add("Snow");
		add("Land");
	}

	static SuperTypes instance = new SuperTypes();
	private LinkedHashMap names;

	private void add(String string) {
		String id = getPrefConstant(string);
		this.names.put(id, string);
	}

	public String getIdPrefix() {
		return "supertypes";
	}

	@Override
	public FilterField getFilterField() {
		return FilterField.CARD_TYPE;
	}

	public static SuperTypes getInstance() {
		return instance;
	}

	public Collection getNames() {
		return new ArrayList(this.names.values());
	}

	public Collection getIds() {
		return new ArrayList(this.names.keySet());
	}

	private String getPrefConstant(String name) {
		return FilterField.getPrefConstant(getIdPrefix(), name);
	}

	public String getNameById(String id) {
		return (String) this.names.get(id);
	}
}
