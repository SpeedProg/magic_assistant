package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;

public class ParseGathererCardLanguagesTest extends TestCase {
	private ParseGathererCardLanguages parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererCardLanguages();
	}

	public void testLoadHtml() {
		parser.setCardId(153981);
		parser.setLanguage("Russian");
		String html = "	        <tr class=\"cardItem oddItem\">\r\n"
				+ "	            <td class=\"fullWidth\" style=\"text-align: center;\">\r\n"
				+ "	                <a id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_languageList_listRepeater_ctl07_cardTitle\" href=\"Details.aspx?multiverseid=172550\">Бурав Выжженной Пустоши</a>\r\n"
				+ "	            </td>\r\n" + "	            <td style=\"text-align: center;\">\r\n" + "	                Russian\r\n"
				+ "	            </td>\r\n" + "	            <td style=\"text-align: center;\">\r\n" + "\r\n"
				+ "	                русский язык\r\n" + "	            </td>\r\n" + "	        </tr>";
		html = html.replaceAll("\r?\n", " ");
		parser.loadHtml(html, new NullProgressMonitor());
		assertEquals(172550, parser.getLangCardId());
	}

	public void testLoad() throws IOException {
		parser.setCardId(153981);
		parser.setLanguage("Russian");
		parser.load(new NullProgressMonitor());
		assertEquals(172550, parser.getLangCardId());
	}
}
