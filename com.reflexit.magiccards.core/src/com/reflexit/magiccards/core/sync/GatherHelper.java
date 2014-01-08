package com.reflexit.magiccards.core.sync;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GatherHelper extends ParserHtmlHelper {
	protected static Pattern countPattern = Pattern
			.compile("Search:<span id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay\"[^>]*><i>[^<]*</i>  \\((\\d+)\\)</span>");
	static String baseSearch = "http://gatherer.wizards.com/Pages/Search/Default.aspx?action=advanced&";
	static Map manaMap = new LinkedHashMap();
	static {
		manaMap.put("\\Q{500}", "{0.5}");
		manaMap.put("\\{(\\d)([BUGRW])\\}", "{$1/$2}");
		manaMap.put("\\{([BUGRW])([BUGRW])\\}", "{$1/$2}");
		manaMap.put("\\Q{tap}", "{T}");
		manaMap.put("\\Q{untap}", "{Q}");
	}

	public GatherHelper() {
		super();
	}

	public static URL getSearchQuery(String output, String set, boolean special) throws MalformedURLException {
		String url;
		if (set != null && set.startsWith("http")) {
			url = set;
		} else {
			String out = "&output=" + output;
			String base = baseSearch + out;
			url = base;
			if (set == null || set.equals("Standard")) {
				url += "&format=[%22Standard%22]";
			} else if (set.equalsIgnoreCase("All")) {
				url += "&set=[%22%22]";
			} else {
				url += "&set=[%22" + set.replaceAll(" ", "%20") + "%22]";
			}
			if (special)
				url += "&special=1";
		}
		return new URL(url);
	}

	public static String htmlToString(String str) {
		str = str.replaceAll("\\Q " + LONG_MINUS, "-");
		str = str.replaceAll("&nbsp;", " ");
		str = str.replaceAll("&amp;", "&");
		str = str.replaceAll("&apos;", "'");
		str = str.replaceAll("&quot;", "\"");
		if (str.contains("img")) {
			str = str.replaceAll("<img [^<]*name=([^&]*)&[^>]*/>", "{$1}");
			for (Iterator iterator = manaMap.keySet().iterator(); iterator.hasNext();) {
				String alt = (String) iterator.next();
				String to = (String) manaMap.get(alt);
				str = str.replaceAll(alt, to);
			}
		}
		return str;
	}
}