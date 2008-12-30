package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class CardTypes implements ISearchableProperty {
	private CardTypes() {
		this.names = new LinkedHashMap();
		add("Land");
		add("Instant");
		add("Sorcery");
		add("Creature");
		add("Enchantment");
		add("Artifact");
		add("Planeswalker");
	}
	static CardTypes instance = new CardTypes();
	private LinkedHashMap names;

	private void add(String string) {
		String id = getPrefConstant(string);
		this.names.put(id, string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.ISearchableProperty#getIdPrefix()
	 */
	public String getIdPrefix() {
		return "types";
	}

	public static CardTypes getInstance() {
		return instance;
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
	public static String[] proposals = new String[] {// 
	"Basic", "Tribal", "World", "Legendary", "Snow",
	// 
	        "Land",
	        "Artifact",
	        "Creature",
	        //
	        "Elf",
	        "Goblin",
	        "Human",
	        "Elemental",
	        "Kithkin" };

	/**
	 * @return
	 */
	public static String[] getProposals() {
		return proposals;
	}
}
