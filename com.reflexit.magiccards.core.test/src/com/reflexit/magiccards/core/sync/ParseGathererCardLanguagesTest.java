package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.Languages.Language;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

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
		parser.loadHtml(html, ICoreProgressMonitor.NONE);
		assertEquals(172550, parser.getLangCardId());
	}

	public void testLoad() throws IOException {
		parser.setCardId(153981);
		parser.setLanguage("Russian");
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals(172550, parser.getLangCardId());
	}

	public void testOtherLangs() throws IOException {
		parser.setCardId(366404);
		parser.setLanguage(Language.CHINESE_SIMPLIFIED.getLang());
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals(365755, parser.getLangCardId());
		parser.setLanguage(Language.KOREAN.getLang());
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals(367343, parser.getLangCardId());
	}

	public void testParseBug379() throws IOException {
		parser.setCardId(179538);
		parser.setLanguage(Language.CHINESE_SIMPLIFIED.getLang());
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals(196420, parser.getLangCardId());
		parser.setCardId(220517);
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals(0, parser.getLangCardIds().size());
	}
}
