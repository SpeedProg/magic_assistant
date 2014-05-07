package com.reflexit.magiccards.core.sync;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.NotNull;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.MagicCard;

public class ParserHtmlHelper {
	public static interface ILoadCardHander {
		void handleCard(MagicCard card);

		void handleSecondary(MagicCard primary, MagicCard secondary);

		void handleEdition(Edition ed);

		void setCardCount(int count);

		int getCardCount();

		int getRealCount();
	}

	static {
		try {
			LONG_MINUS = new String(new byte[] { (byte) 0xe2, (byte) 0x80, (byte) 0x94 }, FileUtils.UTF8);
		} catch (UnsupportedEncodingException e) {
			// hmm
		}
	}

	public static class OutputHandler implements ILoadCardHander {
		private PrintStream out;
		private boolean loadLandPrintings;
		private boolean loadOtherPrintings;
		private int cardCount;
		private int count;

		public OutputHandler(PrintStream st, boolean loadLandPrintings, boolean loadOtherPrintings) {
			this.out = st;
			this.loadLandPrintings = loadLandPrintings;
			this.loadOtherPrintings = loadOtherPrintings;
		}

		public void handleCard(MagicCard card) {
			count++;
			TextPrinter.print(card, this.out);
		}

		public void handleEdition(Edition ed) {
			Edition res = Editions.getInstance().addEdition(ed.getName(), ed.getMainAbbreviation());
			if (res.getReleaseDate() == null)
				res.setReleaseDate(Calendar.getInstance().getTime());
		}

		public void handleSecondary(MagicCard primary, MagicCard secondary) {
			if (loadLandPrintings && primary.getSet() != null && primary.getSet().equals(secondary.getSet())) {
				handleCard(secondary);
			} else if (loadOtherPrintings) {
				handleCard(secondary);
			}
		}

		@Override
		public void setCardCount(int count) {
			this.cardCount = count;
		}

		public int getCardCount() {
			return cardCount;
		}

		@Override
		public int getRealCount() {
			return count;
		}
	}

	protected static GatherHelper.OutputHandler createOutputHandler(PrintStream out, Properties options) {
		String land = (String) options.get(UpdateCardsFromWeb.UPDATE_BASIC_LAND_PRINTINGS);
		boolean bland = "true".equals(land);
		String other = (String) options.get(UpdateCardsFromWeb.UPDATE_OTHER_PRINTINGS);
		boolean bother = "true".equals(other);
		return new GatherHelper.OutputHandler(out, bland, bother);
	}

	public static class StashLoadHandler implements ILoadCardHander {
		private int count;
		private int cardCount;
		private ArrayList<MagicCard> primary = new ArrayList<MagicCard>();
		private ArrayList<MagicCard> secondary = new ArrayList<MagicCard>();

		@Override
		public void setCardCount(int count) {
			cardCount = count;
		}

		@Override
		public void handleSecondary(MagicCard primary, MagicCard secondary) {
			this.secondary.add(secondary);
		}

		@Override
		public void handleEdition(Edition ed) {
			Editions.getInstance().addEdition(ed);
		}

		@Override
		public void handleCard(MagicCard card) {
			primary.add(card);
			count++;
		}

		@Override
		public int getCardCount() {
			return cardCount;
		}

		@Override
		public int getRealCount() {
			return count;
		}

		public Collection<MagicCard> getPrimary() {
			return primary;
		}

		public Collection<MagicCard> getSecondary() {
			return secondary;
		}
	}

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

	protected static String LONG_MINUS;

	/**
	 * If pattern does not match return null, otherwise return group number, or empty string if
	 * group is set to -1
	 * 
	 * @param textPattern
	 * @param line
	 * @param g
	 * @return
	 */
	public static String findMatch(Pattern textPattern, String line, int g) {
		Matcher matcher = textPattern.matcher(line);
		if (matcher.find()) {
			if (g == -1)
				return "";
			return matcher.group(g);
		}
		return null;
	}

	public static int findIntegerMatch(Pattern textPattern, String line, int g, int def) {
		String x = findMatch(textPattern, line, g);
		if (x == null)
			return def;
		try {
			int res = Integer.parseInt(x);
			return res;
		} catch (NumberFormatException e) {
			return def;
		}
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
		String res = GatherHelper.htmlToString(text).trim();
		if (res.length() == 0)
			res = " ";
		return res;
	}

	public ParserHtmlHelper() {
		super();
	}
}