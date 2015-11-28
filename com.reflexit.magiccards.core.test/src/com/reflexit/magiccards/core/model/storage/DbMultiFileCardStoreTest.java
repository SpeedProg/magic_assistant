package com.reflexit.magiccards.core.model.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.xml.DbFileCardStore;
import com.reflexit.magiccards.core.model.xml.DbMultiFileCardStore;
import com.reflexit.unittesting.CardGenerator;

public class DbMultiFileCardStoreTest extends TestCase {
	private static final String MORNINGTIDE = "Morningtide";
	private static final String LORWYN = "Lorwyn";
	DbMultiFileCardStore store;
	protected MagicCard m1;
	protected MagicCard m2;
	private HashMap<String, File> files = new HashMap();

	@Override
	protected void setUp() throws Exception {
		this.store = new DbMultiFileCardStore(false) {
		};
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
		files.put(name, tempFile1);
		this.store.addFile(tempFile1, Location.valueOf(name), false);
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

	public void testGetPrime() {
		this.store.add(m1);
		this.store.add(m2);
		MagicCard m3;
		m3 = (MagicCard) m1.clone();
		m3.setId(String.valueOf(m1.getCardId() + 1));
		m3.setSet(MORNINGTIDE);
		assertEquals(m1, store.getPrime(m1.getName()));
		this.store.add(m3);
		assertEquals(m3, store.getPrime(m1.getName()));
		this.store.add(m1);
		assertEquals(m3, store.getPrime(m1.getName()));
		MagicCard m4;
		m4 = (MagicCard) m1.clone();
		m4.setId(String.valueOf(m3.getCardId() + 1));
		m4.setSet(MORNINGTIDE);
		m4.setEnglishCardId(m3.getCardId());
		this.store.add(m4);
		assertEquals(m3, store.getPrime(m1.getName()));
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
		this.store.initialize();
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

	public void testAddSplit() {
		MagicCard a1 = m1.cloneCard();
		MagicCard a2 = m1.cloneCard();
		this.store.add(a1);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(1, this.store.size());
		MagicCard a3 = m1.cloneCard();
		a3.setProperty(MagicCardField.PART, "test");
		this.store.add(a3);
		assertEquals(2, this.store.size());
		assertEquals(2, this.store.getUniqueCount());
	}

	public void testSaveLoad() {
		MagicCard a1 = m1.cloneCard();
		MagicCard a2 = m1.cloneCard();
		this.store.add(a1);
		a2.setProperty(MagicCardField.PART, "test");
		this.store.add(a2);
		DbFileCardStore store2 = (DbFileCardStore) this.store.getStore(Location.createLocation(LORWYN));
		Iterator<IMagicCard> iterator = store2.iterator();
		assertEquals(a1, iterator.next());
		assertEquals(a2, iterator.next());
	}
}
