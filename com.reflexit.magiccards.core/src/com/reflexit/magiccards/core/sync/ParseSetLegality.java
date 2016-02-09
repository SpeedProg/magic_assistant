package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.exports.HtmlTableImportDelegate;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;

public class ParseSetLegality extends AbstractParseHtmlPage {
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
		String setsHtml = html;
		setsHtml = setsHtml.replaceAll("\r?\n", " ");
		setsHtml = setsHtml.replaceAll("</?b>", "");
		Pattern setPattern = Pattern.compile("<li>\\s*(.+?)\\s*</li>");
		String value = extractPatternValue(setsHtml, setPattern, true);
		String sets[] = value.split("\n");
		Editions eds = Editions.getInstance();
		for (int k = 0; k < sets.length; k++) {
			// 	<li><em>Dragons of Tarkir</em> (effective March 27, 2015)</li>
			String string = sets[k].trim();
			string = string.replaceAll("  ", " ");
			string = string.replaceAll(" *\\(.*", "");
			string = ParserHtmlHelper.purifyItem(string);
			// System.err.println("Looking for " + string);
			if (string.length() > 0) {
				Edition ed = eds.getEditionByName(string);
				if (ed == null) {
					ed = eds.getEditionByNameIgnoreCase(string);
				}
				if (ed != null) {
					ed.setLegalityMap(ed.getLegalityMap().put(format, Legality.LEGAL).complete());
					// System.err.println("Set " + string + " is set to " +
					// format);
				}
			}
		}
	}

	@Override
	protected String getUrl() {
		return "http://magic.wizards.com/en/gameinfo/gameplay/formats/"
				+ format.name().toLowerCase(Locale.ENGLISH);
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		monitor.done();
	}

	private static Collection<String> getFormats() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("Standard");
		res.add("Modern");
		// res.add("Legacy");
		// res.add("Vintage");
		return res;
	}

	public static void main(String[] args) throws IOException {
		ParseSetLegality parser = new ParseSetLegality("Standard");
		parser.load(ICoreProgressMonitor.NONE);
	}
}
