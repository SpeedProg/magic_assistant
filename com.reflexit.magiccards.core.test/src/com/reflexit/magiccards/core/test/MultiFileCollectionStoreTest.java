package com.reflexit.magiccards.core.test;

import java.io.File;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.test.assist.CardGenerator;
import com.reflexit.magiccards.core.xml.CollectionMultiFileCardStore;
import com.reflexit.magiccards.core.xml.SingleFileCardStorage;

public class MultiFileCollectionStoreTest extends junit.framework.TestCase {
	CollectionMultiFileCardStore store;
	protected MagicCard m1;
	protected MagicCard m2;

	@Override
	protected void setUp() throws Exception {
		this.store = new CollectionMultiFileCardStore();
		for (int i = 0; i < 10; i++) {
			File tempFile1 = File.createTempFile("coll" + i, ".xml");
			tempFile1.deleteOnExit();
			(this.store).addFile(tempFile1, "coll" + i, false);
		}
		(this.store).setLocation("coll5");
		this.m1 = CardGenerator.generateRandomCard();
		this.m1.setName("name 1");
		this.m2 = CardGenerator.generateRandomCard();
		this.m2.setName("name 2");
	}

	public void testAddCardFromDB() {
		MagicCard a = CardGenerator.generateRandomCard();
		this.store.add(a);
		assertEquals(this.store.size(), 1);
		boolean found = false;
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
			assertEquals("coll5", ((MagicCardPhisical) card).getLocation());
			found = true;
		}
		assertTrue("Card not found", found);
	}

	public void testAddCardCheckSaved() {
		MagicCard a = CardGenerator.generateRandomCard();
		this.store.add(a);
		String def = this.store.getLocation();
		File tempFile1 = store.getMStorage().getFile(def);
		SingleFileCardStorage loaded = new SingleFileCardStorage(tempFile1, def, true);
		assertEquals(1, loaded.size());
		boolean found = false;
		for (Object element : loaded) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
			assertEquals(def, ((MagicCardPhisical) card).getLocation());
			found = true;
		}
		assertTrue("Card not found", found);
	}

	public void testAddCardWithLocation() {
		MagicCardPhisical a = new MagicCardPhisical(m1);
		a.setLocation("coll2");
		this.store.add(a);
		assertEquals(this.store.size(), 1);
		boolean found = false;
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
			assertEquals("coll2", ((MagicCardPhisical) card).getLocation());
			found = true;
		}
		assertTrue("Card not found", found);
	}

	public void testAddCardWithLocation2() {
		MagicCardPhisical a = new MagicCardPhisical(m1);
		a.setLocation("coll1");
		this.store.add(a);
		((ILocatable) this.store).setLocation("coll2");
		this.store.add(m1);
		assertEquals(this.store.size(), 2);
		Iterator iterator = this.store.iterator();
		IMagicCard card = (IMagicCard) iterator.next();
		assertEquals(a.getCardId(), card.getCardId());
		IMagicCard card2 = (IMagicCard) iterator.next();
		assertEquals(a.getCardId(), card2.getCardId());
		String loc1 = ((MagicCardPhisical) card).getLocation();
		String loc2 = ((MagicCardPhisical) card2).getLocation();
		if (loc1.equals("coll1")) {
			assertEquals("coll1", loc1);
			assertEquals("coll2", loc2);
		} else {
			assertEquals("coll1", loc2);
			assertEquals("coll2", loc1);
		}
	}
}
