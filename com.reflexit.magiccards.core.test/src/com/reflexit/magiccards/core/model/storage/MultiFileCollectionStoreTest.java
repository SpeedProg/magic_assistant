package com.reflexit.magiccards.core.model.storage;

import java.io.File;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.xml.CollectionMultiFileCardStore;
import com.reflexit.magiccards.core.model.xml.CollectionSingleFileCardStore;
import com.reflexit.unittesting.CardGenerator;

public class MultiFileCollectionStoreTest extends junit.framework.TestCase {
	private static final Location LOCATION5 = Location.valueOf("coll5");
	CollectionMultiFileCardStore store;
	protected MagicCard m1;
	protected MagicCard m2;

	@Override
	protected void setUp() throws Exception {
		this.store = new CollectionMultiFileCardStore();
		for (int i = 0; i < 10; i++) {
			File tempFile1 = File.createTempFile("coll" + i, ".xml");
			tempFile1.deleteOnExit();
			(this.store).addFile(tempFile1, Location.valueOf("coll" + i), false);
		}
		(this.store).setLocation(LOCATION5);
		this.m1 = CardGenerator.generateRandomCard();
		this.m1.setName("name 1");
		this.m2 = CardGenerator.generateRandomCard();
		this.m2.setName("name 2");
	}

	private MagicCard createDCard() {
		MagicCard a = CardGenerator.generateRandomCard();
		a.setSet("Elena");
		return a;
	}

	public void testAddCardFromDB() {
		MagicCard a = createDCard();
		this.store.add(a);
		assertEquals(1, this.store.size());
		boolean found = false;
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
			assertEquals(LOCATION5, ((MagicCardPhysical) card).getLocation());
			found = true;
		}
		assertTrue("Card not found", found);
	}

	public void testAddCardCheckSaved() {
		Location def = this.store.getLocation();
		File tempFile1 = store.getFile(def);
		MagicCard a = createDCard();
		this.store.add(a);
		CollectionSingleFileCardStore loaded = new CollectionSingleFileCardStore(tempFile1, def, true);
		assertEquals(1, loaded.size());
		boolean found = false;
		for (Object element : loaded) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
			assertEquals(def, ((MagicCardPhysical) card).getLocation());
			found = true;
		}
		assertTrue("Card not found", found);
	}

	public void testAddCardWithLocation() {
		MagicCardPhysical a = new MagicCardPhysical(m1, null);
		Location loc2 = Location.valueOf("coll2");
		a.setLocation(loc2);
		this.store.add(a);
		assertEquals(this.store.size(), 1);
		boolean found = false;
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
			assertEquals(loc2, ((MagicCardPhysical) card).getLocation());
			found = true;
		}
		assertTrue("Card not found", found);
	}

	public void testAddCardWithLocation2() {
		MagicCardPhysical a = new MagicCardPhysical(m1, null);
		this.store.add(a);
		Location LOC2 = Location.valueOf("coll2");
		((ILocatable) this.store).setLocation(LOC2);
		this.store.add(m1);
		assertEquals(this.store.size(), 2);
		Iterator iterator = this.store.iterator();
		IMagicCard card = (IMagicCard) iterator.next();
		assertEquals(a.getCardId(), card.getCardId());
		IMagicCard card2 = (IMagicCard) iterator.next();
		assertEquals(a.getCardId(), card2.getCardId());
		Location loc1 = ((MagicCardPhysical) card).getLocation();
		Location loc2 = ((MagicCardPhysical) card2).getLocation();
		if (loc1.equals(LOCATION5)) {
			assertEquals(LOCATION5, loc1);
			assertEquals(LOC2, loc2);
		} else {
			assertEquals(LOCATION5, loc2);
			assertEquals(LOC2, loc1);
		}
	}
}
