package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;

public class ParseSetLegality extends ParseGathererPage {
	private Pattern setPattern = Pattern.compile("<li>\\s*<i>(.+?)</i>");
	private String format;

	public ParseSetLegality(String set) {
		this.format = set;
	}

	/*-
	 *   
	 *   <li>
	<i>Mirrodin Besieged</i>
	</li>

	 */
	@Override
	protected void loadHtml(String html, IProgressMonitor monitor) {
		int i = html.indexOf("<h5 class=\"byline\"></h5>");
		int j = html.indexOf("<div class=\"article-bottom\"></div>");
		String setsHtml = html.substring(i, j);
		setsHtml = setsHtml.replaceAll("\r?\n", " ");
		setsHtml = setsHtml.replaceAll("</?b>", "");
		String value = extractPatternValue(setsHtml, setPattern, true);
		String sets[] = value.split("\n");
		Editions eds = Editions.getInstance();
		for (int k = 0; k < sets.length; k++) {
			String string = sets[k].trim();
			string = string.replaceAll("  ", " ");
			if (string.length() > 0) {
				Edition ed = eds.getEditionByName(string);
				if (ed != null)
					ed.addFormat(format);
			}
		}
	}

	@Override
	protected String getUrl() {
		return "http://www.wizards.com/Magic/TCG/Resources.aspx?x=judge/resources/sfr" + format.toLowerCase(Locale.ENGLISH);
	}

	public static void loadAllFormats(IProgressMonitor monitor) {
		Collection<String> formats = getFormats();
		int ticks = 100 * formats.size();
		monitor.beginTask("Updating set formats", ticks);
		try {
			for (Iterator iterator = formats.iterator(); iterator.hasNext();) {
				String format = (String) iterator.next();
				ParseSetLegality parser = new ParseSetLegality(format);
				parser.load(new SubProgressMonitor(monitor, ticks / formats.size()));
			}
			Editions eds = Editions.getInstance();
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
		res.add("Modern");
		res.add("Extended");
		return res;
	}

	public static void main(String[] args) throws IOException {
		ParseSetLegality parser = new ParseSetLegality("Modern");
		parser.load(new NullProgressMonitor());
	}
}
