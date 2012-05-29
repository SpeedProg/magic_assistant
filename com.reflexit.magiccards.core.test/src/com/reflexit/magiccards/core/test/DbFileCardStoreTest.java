package com.reflexit.magiccards.core.test;

import java.io.File;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardCollection;
import com.reflexit.magiccards.core.test.assist.CardGenerator;
import com.reflexit.magiccards.core.xml.DbFileCardStore;

import junit.framework.TestCase;

public class DbFileCardStoreTest extends TestCase {
	ICardCollection<IMagicCard> store;
	protected MagicCard m1;
	protected MagicCard m2;
	protected File tempFile;

	@Override
	protected void setUp() throws Exception {
		Location loc = new Location("aaa.xml");
		tempFile = File.createTempFile("aaa", ".xml");
		tempFile.deleteOnExit();
		this.store = new DbFileCardStore(tempFile, loc, true);
		this.m1 = CardGenerator.generateRandomCard();
		this.m1.setName("name 1");
		this.m2 = CardGenerator.generateRandomCard();
		this.m2.setName("name 2");
	}

	@Override
	protected void tearDown() throws Exception {
		tempFile.delete();
		super.tearDown();
	}

	public void testAddSplit() {
		MagicCard a1 = m1.cloneCard();
		MagicCard a2 = m1.cloneCard();
		this.store.add(a1);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(1, this.store.size());
		a2.setProperty(MagicCardField.PART, "test");
		this.store.add(a2);
		assertEquals(2, this.store.size());
		assertEquals(2, this.store.getCount());
	}
}
