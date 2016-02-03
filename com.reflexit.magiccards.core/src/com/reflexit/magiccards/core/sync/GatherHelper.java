package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.NotNull;

public abstract class GatherHelper extends ParserHtmlHelper {
	protected static Pattern countPattern = Pattern
			.compile("<span id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay\"[^>]*><i>[^<]*</i>\\s*\\((\\d+)\\)\\s*</span>");
	protected static Pattern lastPagePattern = Pattern
			.compile("\\Q<span style=\"visibility:hidden;\">&nbsp;&gt;</span></div>");
	static String baseSearch = "http://gatherer.wizards.com/Pages/Search/Default.aspx?action=advanced&";
	static Map manaMap = new LinkedHashMap();
	static {
		manaMap.put("\\Q{500}", "{0.5}");
		manaMap.put("\\{(\\d)([BUGRW])\\}", "{$1/$2}");
		manaMap.put("\\{([BUGRW])([BUGRW])\\}", "{$1/$2}");
		manaMap.put("\\Q{tap}", "{T}");
		manaMap.put("\\Q{untap}", "{Q}");
	}

	public static final String GATHERER_URL_BASE = "http://gatherer.wizards.com/";

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

	public static void saveManaSymbol(File dir, String name) throws MalformedURLException {
		URL url = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?size=small&type=symbol&name="
				+ name);
		try {
			InputStream st = WebUtils.openUrl(url);
			File f = new File(dir, "Symbol_" + name + "_mana.gif");
			FileUtils.saveStream(st, f);
			st.close();
			if (f.length() == 0) {
				System.err.println("Error " + f);
				f.delete();
			} else
				System.err.println("Saved " + f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@NotNull
	public static URL createImageURL(int cardId) {
		try {
			return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + cardId
					+ "&type=card");
		} catch (MalformedURLException e) {
			throw new MagicException(e);
		}
	}

	public static URL createImageDetailURL(int cardId) {
		try {
			return new URL(ParseGathererOracle.DETAILS_QUERY_URL_BASE + cardId);
		} catch (MalformedURLException e) {
			throw new MagicException(e);
		}
	}

	public static int extractCardIdFromURL(URL url) {
		String query = url.getQuery();
		Pattern pattern = Pattern.compile("multiverseid=(-*\\d+)");
		Matcher matcher = pattern.matcher(query);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

	public static URL createSetImageURL(String editionAbbr, String rarity) {
		try {
			String rarLetter = rarity == null ? "C" : rarity.substring(0, 1).toUpperCase();
			return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=" + editionAbbr
					+ "&size=small&rarity="
					+ rarLetter);
		} catch (MalformedURLException e) {
			throw new MagicException(e);
		}
	}

	public static URL createManaImageURL(String symbol) {
		String manaName = symbol.replaceAll("[{}/]", "");
		try {
			return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=" + manaName
					+ "&type=symbol");
		} catch (MalformedURLException e) {
			return null;
		}
	}
}