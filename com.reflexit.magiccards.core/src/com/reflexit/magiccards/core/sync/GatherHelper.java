package com.reflexit.magiccards.core.sync;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.NotNull;

public class GatherHelper {
	public static Charset UTF_8 = Charset.forName("utf-8");

	@NotNull
	public static URL createImageURL(int cardId, String editionAbbr) throws MalformedURLException {
		return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + cardId + "&type=card");
	}

	public static URL createSetImageURL(String editionAbbr, String rarity) {
		try {
			String rarLetter = rarity == null ? "C" : rarity.substring(0, 1).toUpperCase();
			return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=" + editionAbbr + "&size=small&rarity="
					+ rarLetter);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static URL createManaImageURL(String symbol) {
		String manaName = symbol.replaceAll("[{}/]", "");
		try {
			return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=" + manaName + "&type=symbol");
		} catch (MalformedURLException e) {
			return null;
		}
	}

	static Map manaMap = new LinkedHashMap();
	static {
		manaMap.put("\\Q{500}", "{0.5}");
		manaMap.put("\\{(\\d)([BUGRW])\\}", "{$1/$2}");
		manaMap.put("\\{([BUGRW])([BUGRW])\\}", "{$1/$2}");
		manaMap.put("\\Q{tap}", "{T}");
		manaMap.put("\\Q{untap}", "{Q}");
	}
	private static String LONG_MINUS;
	static {
		try {
			LONG_MINUS = new String(new byte[] { (byte) 0xe2, (byte) 0x80, (byte) 0x94 }, UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			// hmm
		}
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

	public GatherHelper() {
		super();
	}

	public static String getMatch(Pattern textPattern, String line) {
		return getMatch(textPattern, line, 1);
	}

	public static String getMatch(Pattern textPattern, String line, int g) {
		Matcher matcher;
		matcher = textPattern.matcher(line);
		String text = "";
		if (matcher.find()) {
			text = matcher.group(g);
			if (text == null)
				text = "";
		}
		String res = htmlToString(text).trim();
		if (res.length() == 0)
			res = " ";
		return res;
	}
}