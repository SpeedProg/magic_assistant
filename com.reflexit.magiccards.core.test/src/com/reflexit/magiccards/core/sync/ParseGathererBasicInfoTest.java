package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.reflexit.magiccards.core.model.MagicCard;

public class ParseGathererBasicInfoTest extends TestCase {
	private ParseGathererBasicInfo parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererBasicInfo();
	}

	public void testLoad() throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(172550);
		parser.setCard(card);
		parser.load(new NullProgressMonitor());
		assertEquals("Бурав Выжженной Пустоши", card.getName());
	}

	public void testInnistradSide2Russian() throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(273275);
		parser.setCard(card);
		parser.load(new NullProgressMonitor());
		assertEquals("149b", card.getCollNumber());
		assertEquals("Wildblood Pack", card.getName()); // XXX bug in gatherer
	}
}
