package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.reflexit.magiccards.core.model.MagicCard;

public class ParseGathererRulingsTest extends TestCase {
	private ParseGathererRulings parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererRulings();
	}

	// public void testLoadHtml() {
	// parser.setCardId(153981);
	// parser.setLanguage("Russian");
	// String html = "";
	// html = html.replaceAll("\r?\n", " ");
	// parser.loadHtml(html, new NullProgressMonitor());
	// assertEquals(172550, parser.getLangCardId());
	// }
	public void testLoad() throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(191338);
		parser.setCard(card);
		parser.load(new NullProgressMonitor());
		assertEquals(220, Integer.parseInt(card.getCollNumber()));
	}
}
