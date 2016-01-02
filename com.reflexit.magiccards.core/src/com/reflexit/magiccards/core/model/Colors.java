package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.MagicLogger;

public class Colors implements ISearchableProperty {
	static Colors instance = new Colors();
	private LinkedHashMap<String, ManaColor> idmap;
	private HashMap<String, ManaColor> namemap;
	private final static Pattern colorpattern = Pattern.compile("\\b([WUBRG])P?\\b");

	public static enum ManaColor {
		WHITE("W"), BLUE("U"), BLACK("B"), RED("R"), GREEN("G"), COLORLESS("1");
		String tag;
		String label;

		ManaColor(String tag) {
			this.tag = tag;
			this.label = name().substring(0, 1) + name().substring(1).toLowerCase(Locale.ENGLISH);
		}

		public String tag() {
			return tag;
		}

		public String getLabel() {
			return label;
		}

		public String getPrefId() {
			return FilterField.getPrefConstant(FilterField.COLOR.toString(), getLabel());
		}

		static ManaColor valueOfTag(String tag) {
			for (ManaColor c : values()) {
				if (c.tag.equals(tag))
					return c;
			}
			return null;
		}
	};

	private Colors() {
		this.idmap = new LinkedHashMap<String, ManaColor>();
		this.namemap = new HashMap<String, ManaColor>();
		for (ManaColor c : ManaColor.values()) {
			idmap.put(c.getPrefId(), c);
			namemap.put(c.getLabel(), c);
		}
	}

	public Collection<String> getColorIdentity(IMagicCard card) {
		Collection<String> res = new LinkedHashSet<String>();
		try {
			getColorPresense(card.getCost(), res);
			getColorPresense(card.getOracleText(), res);
			getColorPresense(getEncodeByName((String) card.get(MagicCardField.COLOR_INDICATOR)), res);
		} catch (Exception e) {
			MagicLogger.log("Cannot get identity for card: " + e);
		}
		return res;
	}

	public static Collection<String> getColorPresense(String text) {
		return getColorPresense(text, new LinkedHashSet<String>());
	}

	public static String getColorAsCost(IMagicCard card) {
		String text = card.getCost();
		if (text == null || text.isEmpty())
			return "";
		return toCost(getColorPresense(text, new LinkedHashSet<String>()));
	}

	public String getColorIdentityAsCost(IMagicCard card) {
		return toCost(getColorIdentity(card));
	}

	public static Collection<String> getColorPresense(String text, Collection<String> res) {
		if (text == null || text.length() == 0)
			return res;
		Matcher matcher = colorpattern.matcher(text);
		while (matcher.find()) {
			res.add(matcher.group(1));
		}
		return res;
	}

	public static String getColorName(String cost) {
		if (cost == null)
			return "Unknown";
		if (cost.length() == 0)
			return "No Cost";
		Collection<String> colorIdentity = sortTags(getColorPresense(cost));
		StringBuffer buf = new StringBuffer();
		for (String c : colorIdentity) {
			String name = ManaColor.valueOfTag(c).getLabel();
			if (buf.length() > 0) {
				buf.append('-');
			}
			buf.append(name);
		}
		String res = buf.toString();
		if (res.length() == 0)
			return "Colorless";
		return res;
	}

	public static int getColorSort(String cost) {
		if (cost == null)
			return 0;
		if (cost.length() == 0)
			return 0;
		if (cost.equals("*"))
			return 0b111111;
		int sum = 1;
		int times = 0;
		Collection<String> colorIdentity = getColorPresense(cost);
		for (ManaColor c : ManaColor.values()) {
			sum <<= 1;
			if (colorIdentity.contains(c.tag())) {
				times++;
				sum |= 0b1;
			}
		}
		if (sum == 0)
			return 1;
		if (times == 1)
			sum <<= 7;
		return -sum;
	}

	@Override
	public String getIdPrefix() {
		return getFilterField().toString();
	}

	@Override
	public FilterField getFilterField() {
		return FilterField.COLOR;
	}

	public static Colors getInstance() {
		return instance;
	}

	@Override
	public Collection<String> getIds() {
		return new ArrayList<String>(this.idmap.keySet());
	}

	public String getPrefConstant(String name) {
		return FilterField.getPrefConstant(getIdPrefix(), name);
	}

	@Override
	public String getNameById(String id) {
		return this.idmap.get(id).getLabel();
	}

	public String getEncodeByName(String r) {
		try {
			if (r == null || r.charAt(0) == '*')
				return "";
			if (r.contains(",")) {
				String colors[] = r.split(", *");
				String res = "";
				for (String ecolor : colors) {
					if (res.length() > 0)
						res += "/";
					res += namemap.get(ecolor).tag();
				}
				return res;
			}
			return this.namemap.get(r).tag();
		} catch (NullPointerException e) {
			MagicLogger.log("Unknown color: " + r);
			return "";
		}
	}

	public String getCostByName(String r) {
		try {
			if (r == null || r.charAt(0) == '*' | r.equals("No Cost"))
				return "";
			String colors[] = r.split("-");
			String res = "";
			for (String ecolor : colors) {
				res += "{" + namemap.get(ecolor).tag() + "}";
			}
			return res;
		} catch (NullPointerException e) {
			MagicLogger.log("Unknown color: " + r);
			return "";
		}
	}

	public static String getColorType(String cost) {
		if (cost == null || cost.length() == 0)
			return "land";
		if (cost.contains("/"))
			return "hybrid";
		Collection<String> colors = getColorPresense(cost);
		int diff = colors.size();
		if (diff == 0)
			return "colorless";
		if (diff == 1)
			return "mono";
		return "multi";
	}

	public int getConvertedManaCost(String cost) {
		if (cost == null || cost.length() == 0)
			return 0;
		String[] manas = manasplit(cost);
		int res = 0;
		for (String x : manas) {
			if (x.equals("X") || x.equals("Y") || x.equals("Z")) {
				res += 0;
				continue;
			}
			if (Character.isDigit(x.charAt(0))) {
				try {
					int a = x.indexOf('/');
					if (a > 0) {
						res += Integer.parseInt(x.substring(0, a));
						continue;
					}
					res += Integer.parseInt(x);
					continue;
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			res++;
		}
		return res;
	}

	public static String[] manasplit(String cost) {
		if (cost.contains(","))
			return new String[] { "1000000" };
		int k = 0;
		for (int i = 0; i < cost.length(); i++) {
			if (cost.charAt(i) == '{') {
				k++;
			}
		}
		String manas[] = new String[k];
		int ik = 0;
		for (int i = 0; i < cost.length(); i++) {
			if (cost.charAt(i) == '{') {
				int l = cost.indexOf('}', i);
				manas[ik++] = cost.substring(i + 1, l);
				i = l;
			}
		}
		return manas;
	}

	public static List<String> sortTags(Collection<String> tags) {
		ArrayList<String> res = new ArrayList<>();
		for (ManaColor c : ManaColor.values()) {
			if (tags.contains(c.tag()))
				res.add(c.tag());
		}
		return res;
	}

	public String toCost(ManaColor... colors) {
		String res = "";
		for (ManaColor c : ManaColor.values()) {
			for (ManaColor manaColor : colors) {
				if (manaColor == c) {
					res += "{" + c.tag() + "}";
					break;
				}
			}
		}
		return res;
	}

	public static String toCost(Collection<String> tags) {
		String res = "";
		for (ManaColor c : ManaColor.values()) {
			for (String manaColor : tags) {
				if (c.tag().equals(manaColor)) {
					res += "{" + manaColor + "}";
					break;
				}
			}
		}
		return res;
	}
}
