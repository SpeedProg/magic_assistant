package com.reflexit.magiccards.core.seller;

import org.eclipse.core.runtime.IProgressMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;

public class ParseMtgFanaticPrices {
	String baseURL;
	String setURL;

	public ParseMtgFanaticPrices() {
		// http://www.mtgfanatic.com/store/viewproducts.aspx?CatID=217&AffiliateID=44349&PageSize=500
		baseURL = "http://www.mtgfanatic.com/store/viewproducts.aspx?CatID=SET&AffiliateID=44349&PageSize=500";
		setURL = "http://www.mtgfanatic.com/Store/Magic/BasicSearch.aspx?CatID=3";
	}

	public void updateStore(IFilteredCardStore<IMagicCard> fstore, IProgressMonitor monitor) throws IOException {
		monitor.beginTask("Loading prices...", fstore.getSize() + 10);
		HashSet<String> sets = new HashSet();
		for (IMagicCard magicCard : fstore) {
			String set = magicCard.getSet();
			sets.add(set);
		}
		HashMap<String, String> parseSets = parseSets();
		monitor.worked(10);
		for (String set : sets) {
			String id = findSetId(set, parseSets);
			if (monitor.isCanceled())
				return;
			if (id != null) {
				System.err.println("found " + set + " " + id);
				HashMap<String, Float> prices = parse(id);
				if (prices.size() > 0) {
					IStorage storage = null;
					if (fstore.getCardStore() instanceof IStorageContainer) {
						storage = ((IStorageContainer) fstore.getCardStore()).getStorage();
						storage.setAutoCommit(false);
					}
					try {
						for (IMagicCard magicCard : fstore) {
							if (monitor.isCanceled())
								return;
							String set2 = magicCard.getSet();
							if (set2.equals(set)) {
								if (prices.containsKey(magicCard.getName())) {
									Float price = prices.get(magicCard.getName());
									if (magicCard instanceof MagicCardPhisical) {
										((MagicCardPhisical) magicCard).setDbPrice(price);
									} else if (magicCard instanceof MagicCard) {
										((MagicCard) magicCard).setDbPrice(price);
									}
									fstore.getCardStore().update(magicCard);
									monitor.worked(1);
								}
							}
						}
					} finally {
						if (storage != null) {
							storage.save();
							storage.setAutoCommit(true);
						}
						monitor.done();
					}
				}
			}
		}
	}

	private String findSetId(String set, HashMap<String, String> parseSets) {
		String id = parseSets.get(set);
		if (id != null)
			return id;
		if (set.equals("Ravnica: City of Guilds"))
			return parseSets.get("Ravnica");
		if (set.equals("Judgment"))
			return parseSets.get("Judgement");
		if (set.equals("Tenth Edition"))
			return parseSets.get("10th Edition");
		if (set.equals("Ninth Edition"))
			return parseSets.get("9th Edition");
		if (set.equals("Eighth Edition"))
			return parseSets.get("8th Edition");
		if (set.equals("Seventh Edition"))
			return parseSets.get("7th Edition");
		if (set.equals("Classic Sixth Edition"))
			return parseSets.get("6th Edition");
		if (set.equals("Fifth Edition"))
			return parseSets.get("5th Edition");
		if (set.equals("Fourth Edition"))
			return parseSets.get("4th Edition");
		if (set.equals("Portal Second Age"))
			return parseSets.get("Portal: Second Age");
		if (set.equals("Portal Three Kingdoms"))
			return parseSets.get("Portal: Three Kingdoms");
		System.err.println("Cannot find prices for " + set);
		return null;
	}

	public HashMap<String, Float> parse(String setId) throws IOException {
		HashMap<String, Float> res = new HashMap<String, Float>();
		URL url = new URL(baseURL.toString().replace("SET", setId));
		InputStream openStream = url.openStream();
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream));
		processFile(st, res);
		st.close();
		return res;
	}
	private static final Pattern rowStart = Pattern.compile("<tr id=\"r\\d+\" class=\"(alt)?[Dd]ataRow\">");
	private static final Pattern rowEnd = Pattern.compile("</tr>");

	private void processFile(BufferedReader st, HashMap<String, Float> res) throws IOException {
		String line = "";
		while ((line = st.readLine()) != null) {
			if (rowStart.matcher(line).find()) {
				String row = "";
				while ((line = st.readLine()) != null) {
					if (rowEnd.matcher(line).find())
						break;
					row = row + line;
				}
				processRow(row, res);
			}
		}
	}
	private static final Pattern rowPattern = Pattern.compile("<td>" // 
	        + "\\s*<a href=\"/store/magic/ViewCard.aspx.I=MTG-MS-\\d+\">" // 
	        + "\\s*(.*) - (.*?)" // 
	        + "\\s*</a>" // 
	        + "\\s*</td>" // 
	        + "\\s*<td>\\$([0-9.]+)</td>" //
	);

	private void processRow(String row, HashMap<String, Float> res) {
		Matcher matcher = rowPattern.matcher(row);
		if (matcher.find()) {
			String name = matcher.group(2);
			String price = matcher.group(3);
			try {
				float f = Float.parseFloat(price);
				res.put(name, f);
			} catch (NumberFormatException e) {
				return;
			}
		}
	}
	private static final Pattern setLinePattern = Pattern.compile("class=\"clMenu\"");
	private static final Pattern setItemPattern = Pattern
	        .compile("<li><a href=\"/store/default.aspx.CatID=(\\d+)\">\\s*(.*?)</a></li>");

	public HashMap<String, String> parseSets() throws IOException {
		HashMap<String, String> res = new HashMap<String, String>();
		URL url = new URL(setURL);
		InputStream openStream = url.openStream();
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream));
		processSetFile(st, res);
		st.close();
		return res;
	}

	private void processSetFile(BufferedReader st, HashMap<String, String> res) throws IOException {
		String line = "";
		while ((line = st.readLine()) != null) {
			if (setLinePattern.matcher(line).find()) {
				Matcher m = setItemPattern.matcher(line);
				while (m.find()) {
					String id = m.group(1);
					String name = m.group(2);
					res.put(name, id);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		ParseMtgFanaticPrices item = new ParseMtgFanaticPrices();
		//HashMap<String, Float> res = item.parse();
		//System.err.println(res);
		item.parseSets();
	}
}
