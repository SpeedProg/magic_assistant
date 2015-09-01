package com.reflexit.magiccards.core.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SpecialTags {
	private static SpecialTags instance = new SpecialTags();

	public static SpecialTags getInstance() {
		return instance;
	}

	private final HashSet<String> conditions;
	private final HashSet<String> usedTags;

	private SpecialTags() {
		conditions = new HashSet<String>();
		usedTags = new HashSet<String>();
		conditions.addAll(Arrays.asList(new String[] { "mint", "nearmint", "played", "poor", "good" }));
		usedTags.addAll(Arrays.asList(new String[] { "foil", "premium", "promo", "online", "signed",
				"textless",
				"wishlist", "fortrade" }));
		usedTags.addAll(conditions);
	}

	public static String[] getTags() {
		HashSet<String> usedTags2 = SpecialTags.getInstance().usedTags;
		return usedTags2.toArray(new String[usedTags2.size()]);
	}

	public Map<String, String> toMap(IMagicCard card) {
		String speValue = card.getString(MagicCardField.SPECIAL);
		return toMap(speValue);
	}

	public Map<String, String> toMap(String speValue) {
		if (speValue == null)
			return Collections.emptyMap();
		if (speValue.length() == 0)
			return Collections.emptyMap();
		String tags[] = speValue.split(",");
		Map<String, String> map = new LinkedHashMap<String, String>(tags.length);
		for (int i = 0; i < tags.length; i++) {
			String tag = tags[i];
			int k = tag.indexOf('=');
			if (k >= 0) {
				String key = tag.substring(0, k);
				String value = tag.substring(k + 1).trim();
				if (key.length() > 0 && value.length() > 0) {
					addTag(map, key, value);
				}
			} else {
				if (tag.startsWith("+")) {
					tag = tag.substring(1);
					addTag(map, tag, "true");
				} else if (tag.startsWith("-")) {
					tag = tag.substring(1);
					addTag(map, tag, "false");
				} else
					addTag(map, tag, "true");
			}
		}
		return map;
	}

	public void modifySpecial(MagicCardPhysical card, String special) {
		if (special == null || special.trim().length() == 0) {
			card.setProperty(MagicCardField.SPECIAL, null);
			return;
		}
		Map<String, String> map1 = toMap(card);
		Map<String, String> map2 = toMap(special);
		Map<String, String> map3 = new LinkedHashMap<String, String>(map1);
		map3.putAll(map2);
		String value = toString(map3);
		card.setProperty(MagicCardField.SPECIAL, value);
	}

	private String toString(Map<String, String> map) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			String value = map.get(key);
			if (value.equals("true") || value.trim().isEmpty()) {
				if (builder.length() > 0)
					builder.append(",");
				builder.append(key);
			} else if (value.equals("false")) {
				// skip
				continue;
			} else {
				if (builder.length() > 0)
					builder.append(",");
				builder.append(key);
				builder.append("=");
				builder.append(value);
			}
		}
		return builder.toString();
	}

	private void addTag(Map<String, String> map, String key, String value) {
		key = key.trim();
		if (conditions.contains(key)) {
			map.put("c", key);
		} else {
			usedTags.add(key);
			map.put(key, value);
		}
	}

	// private void removeTag(Map<String, String> map, String key) {
	// if (conditions.contains(key)) {
	// map.remove("c");
	// } else {
	// map.remove(key);
	// }
	// }

	public String getSpecialValue(String string, String key) {
		Map<String, String> map = toMap(string);
		return map.get(key);
	}
}
