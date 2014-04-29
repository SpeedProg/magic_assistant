package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;

public class ParseSetLegality extends ParseGathererPage {
	private Pattern setPattern = Pattern.compile("<li>\\s*<i>(.+?)</i>");
	private Format format;

	public ParseSetLegality(String format) {
		this.format = Format.valueOf(format);
	}

	/*-
	 *   
	 *   <li>
	<i>Mirrodin Besieged</i>
	</li>

	 */
	@Override
	protected void loadHtml(String html, ICoreProgressMonitor monitor) {
		int i = html.indexOf("<div class=\"article-content\">");
		int j = html.indexOf("<div class=\"article-bottom\">");
		String setsHtml = html.substring(i, j);
		setsHtml = setsHtml.replaceAll("\r?\n", " ");
		setsHtml = setsHtml.replaceAll("</?b>", "");
		String value = extractPatternValue(setsHtml, setPattern, true);
		String sets[] = value.split("\n");
		Editions eds = Editions.getInstance();
		for (int k = 0; k < sets.length; k++) {
			String string = sets[k].trim();
			string = string.replaceAll("  ", " ");
			// System.err.println("Looking for " + string);
			if (string.length() > 0) {
				Edition ed = eds.getEditionByName(string);
				if (ed == null) {
					ed = eds.getEditionByNameIgnoreCase(string);
				}
				if (ed != null) {
					ed.getLegalityMap().put(format, Legality.LEGAL);
					ed.getLegalityMap().complete();
					// System.err.println("Set " + string + " is set to " + format);
				}
			}
		}
	}

	@Override
	protected String getUrl() {
		return "http://www.wizards.com/Magic/TCG/Resources.aspx?x=judge/resources/sfr" + format.name().toLowerCase(Locale.ENGLISH);
	}

	public static void loadAllFormats(ICoreProgressMonitor monitor) {
		Collection<String> formats = getFormats();
		int ticks = 100 * formats.size();
		monitor.beginTask("Updating set formats", ticks);
		try {
			Editions eds = Editions.getInstance();
			for (Edition ed : eds.getEditions()) {
				ed.setFormats("");
			}
			for (Iterator iterator = formats.iterator(); iterator.hasNext();) {
				String format = (String) iterator.next();
				ParseSetLegality parser = new ParseSetLegality(format);
				parser.load(new SubCoreProgressMonitor(monitor, ticks / formats.size()));
			}
			eds.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		monitor.done();
	}

	private static Collection<String> getFormats() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("Standard");
		res.add("Extended");
		res.add("Modern");
		// res.add("Legacy");
		// res.add("Vintage");
		return res;
	}

	public static void main(String[] args) throws IOException {
		ParseSetLegality parser = new ParseSetLegality("Modern");
		parser.load(ICoreProgressMonitor.NONE);
	}
}
