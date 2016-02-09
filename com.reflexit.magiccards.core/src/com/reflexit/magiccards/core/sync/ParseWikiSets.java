/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *    Terry Long - refactored ParseGathererLegality to instead retrieve rulings on cards
 *
 *******************************************************************************/
package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Retrieve legality info
 */
public class ParseWikiSets extends AbstractParseHtmlPage {
	private static final String SET_QUERY_URL_BASE = "https://en.wikipedia.org/wiki/";
	private Collection<String> allParsed = new LinkedHashSet<String>();

	@Override
	protected String getUrl() {
		try {
			String setE = URLEncoder.encode("List_of_Magic:_The_Gathering_sets", "UTF-8");
			return SET_QUERY_URL_BASE + setE;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public synchronized void load(ICoreProgressMonitor monitor) throws IOException {
		try {
			super.load(monitor);
		} catch (Throwable e) {
			// cannot throw anything, this is optional loader
			MagicLogger.log(e);
		}
	}

	@Override
	protected void loadHtml(String html, ICoreProgressMonitor monitor) {
		Editions editions = Editions.getInstance();
		doRun(html);
	}

	protected void doRun(String text) {
		text = text.replaceAll("\r?\n", " ");
		String[] tables = text.split("<table[> ]");
		if (tables.length <= 1)
			throw new MagicException("Tag <table> is not found");
		// 1 Base/core set editions
		// 2 Expansion sets
		// 3 Compilations/reprint sets
		// 4 Non stanard
		// 5 Intro
		loadTable(tables[1], "Core");
		loadTable(tables[2], "Expansion");
		loadTable(tables[3], "Starter");
		loadTable(tables[4], "Expansion");
		loadTable(tables[5], "Starter");
		loadTable(tables[6], "Un_set");
		loadTable(tables[7], "Online");
		Editions editions = Editions.getInstance();
		for (Edition ed : editions.getEditions()) {
			if (!allParsed.contains(ed.getName())) {
				System.err.println("Error: Not found on the wiki " + ed);
			}
		}
	}

	protected void loadTable(String string, String type) {
		List<String> lines = splitArray("tr", string);
		List<String> headers = null;
		int setIndex = 0;
		int setCode = 2;
		int releaseIndex = 4;
		String block = null;
		SimpleDateFormat formatter = new SimpleDateFormat("MMMMMM dd, yyyy", Locale.ENGLISH);
		SimpleDateFormat formatter2 = new SimpleDateFormat("MMMMMM yyyy", Locale.ENGLISH);
		Editions editions = Editions.getInstance();
		for (String line : lines) {
			if (line.contains("<th")) {
				List<String> ths = splitArray("th", line);
				if (headers == null) {
					headers = purifyList(ths);
					// System.err.println("th -> " + headers);
					for (int i = 0; i < headers.size(); i++) {
						if (headers.get(i).equalsIgnoreCase("Release date")) {
							releaseIndex = i;
						}
					}
				}
			} else {
				List<String> tbody = splitArray("td", line);
				List<String> columns = purifyList(tbody);
				if (columns.size() < 5) {
					String name = columns.get(setIndex);
					name = name.replaceAll(" Cycle/Block", " Block");
					name = name.replaceAll(".* or ", "");
					// name = name.replaceAll(" Block", "");
					block = name;
					// System.err.println(columns);
				} else {
					String name = columns.get(setIndex);
					allParsed.add(name);
					String code = columns.get(setCode);
					String date = columns.get(releaseIndex);
					Edition edition = editions.getEditionByName(name);
					if (edition == null) {
						if (code.contains("unrev") || code.contains("none") || code.contains("N/A")
								|| code.contains("unknown"))
							continue;
						Edition editionByAbbr = editions.getEditionByAbbr(code);
						String locname = "";
						if (editionByAbbr != null) {
							locname = editionByAbbr.getName();
						}
						System.err.println("Error: Cannot find " + name + " [" + code + "] " + locname);
						continue;
					}
					String oldMain = edition.getMainAbbreviation();
					if (!oldMain.equals(code) && !code.contains("/")) {
						System.err.println("Error: Main abbreviation mismatch " + code + " " + edition + " " + oldMain);
						edition.setMainAbbreviation(code);
						// File file = new File(
						// "/home/elaskavaia/git/mtgbrowser-git/com.reflexit.magiccards.db/resources/",
						// oldMain + ".txt");
						// if (file.exists())
						// file.renameTo(new File(file.getParentFile(), code + ".txt"));
					}
					try {
						Date newDate = formatter.parse(date);
						edition.setReleaseDate(newDate);
					} catch (ParseException e) {
						try {
							Date newDate = formatter2.parse(date);
							edition.setReleaseDate(newDate);
						} catch (ParseException e2) {
							System.err.println("Error: " + e2.getMessage());
						}
					}
					if (!edition.getType().equals(type)) {
						System.err.println("Error: Type mismatch " + type + " " + edition + " " + edition.getType());
						edition.setType(type);
					}
					if (block != null && !edition.getBlock().equals(block)) {
						// System.err.println(
						// "mismatch block '" + block + "' " + edition + " was '" + edition.getBlock() + "'");
						edition.setBlock(block);
					}
					// System.err.println(name + " " + code + " " + date);
				}
			}
		}
	}

	public static List<String> purifyList(List<String> tbody) {
		List<String> fields = new ArrayList<String>(tbody.size());
		for (String in : tbody) {
			fields.add(purifyItem(in));
		}
		return fields;
	}

	public static String purifyItem(String in) {
		String res = ParserHtmlHelper.consume("sup", in);
		while (!res.equals(in)) {
			in = res;
			res = ParserHtmlHelper.consume("sup", in);
		}
		in = res;
		in = in.replaceAll("<[^>]*>", "");
		in = in.replace("&nbsp;", " ");
		in = in.replace("&amp;", "&");
		in = in.trim();
		return in;
	}

	protected List<String> splitArray(String tag, String text) {
		return ParserHtmlHelper.splitArray(tag, text);
	}

	public static void main(String[] args) throws IOException {
		// card.setCardId(11179);
		ParseWikiSets parser = new ParseWikiSets();
		parser.load(ICoreProgressMonitor.NONE);
		// File file = new File("/tmp/setlist.html");
		// String html = FileUtils.readFileAsString(file);
		// parser.doRun(html);
		Editions editions = Editions.getInstance();
		try {
			editions.save(new File("/tmp/madatabase/editions.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
