package com.reflexit.magiccards.core.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.test.assist.CardGenerator;
import com.reflexit.magiccards.core.xml.CollectionMultiFileCardStore;
import com.reflexit.magiccards.core.xml.VirtualMultiFileCardStore;

public class VirtualMultiFileCardStoreTest extends TestCase {
	private static final String MORNINGTIDE = "Morningtide";
	private static final String LORWYN = "Lorwyn";
	CollectionMultiFileCardStore store;
	protected MagicCard m1;
	protected MagicCard m2;

	@Override
	protected void setUp() throws Exception {
		this.store = new VirtualMultiFileCardStore();
		addStore(LORWYN);
		addStore(MORNINGTIDE);
		this.m1 = CardGenerator.generateRandomCard();
		this.m1.setName("name 1");
		this.m1.setSet(LORWYN);
		this.m2 = CardGenerator.generateRandomCard();
		this.m2.setName("name 2");
		this.m2.setSet(MORNINGTIDE);
	}

	private void addStore(String name) throws IOException {
		File tempFile1 = File.createTempFile(name, ".xml");
		tempFile1.deleteOnExit();
		this.store.addFile(tempFile1, name, false);
	}

	public void testAddCard() {
		this.store.add(m1);
		assertEquals(this.store.size(), 1);
		boolean found = false;
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertTrue(card instanceof MagicCard);
			assertEquals(m1.getCardId(), card.getCardId());
			assertEquals(LORWYN, ((MagicCard) card).getSet());
			found = true;
		}
		assertTrue("Card not found", found);
	}

	public void testAddAll() {
		ArrayList co = new ArrayList();
		co.add(m1);
		co.add(m2);
		this.store.addAll(co);
		assertEquals(this.store.size(), 2);
		int found = 0;
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertTrue(card instanceof MagicCard);
			if (m1.getCardId() == card.getCardId()) {
				assertEquals(LORWYN, ((MagicCard) card).getSet());
				found++;
			} else if (m2.getCardId() == card.getCardId()) {
				assertEquals(MORNINGTIDE, ((MagicCard) card).getSet());
				found++;
			}
		}
		assertEquals("Card not found", 2, found);
		this.store.doInitialize();
		found = 0;
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertTrue(card instanceof MagicCard);
			if (m1.getCardId() == card.getCardId()) {
				assertEquals(LORWYN, ((MagicCard) card).getSet());
				found++;
			} else if (m2.getCardId() == card.getCardId()) {
				assertEquals(MORNINGTIDE, ((MagicCard) card).getSet());
				found++;
			}
		}
		assertEquals("Card not found", 2, found);
	}
}
