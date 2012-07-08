package com.reflexit.magiccards.core.model;

public class SpecialTags {
	private final static String[] tags = new String[] { "mint", "nearmint", "played", "foil", "premium", "promo", "online" };

	public static String[] getTags() {
		return tags;
	}
}
