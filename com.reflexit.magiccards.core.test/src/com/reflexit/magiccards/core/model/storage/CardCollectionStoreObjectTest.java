package com.reflexit.magiccards.core.model.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.xml.CardCollectionStoreObject;
import com.reflexit.unittesting.CardGenerator;

public class CardCollectionStoreObjectTest extends TestCase {
	protected MagicCard m1;
	protected MagicCardPhysical mcp;
	protected File tempFile;
	CardCollectionStoreObject store;
	CardCollectionStoreObject store2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tempFile = File.createTempFile("deck", ".xml");
		tempFile.deleteOnExit();
		store = CardCollectionStoreObject.initFromFile(tempFile);
		store.list = new ArrayList();
		store.name = "bla";
		m1 = CardGenerator.generateCardWithValues();
		mcp = CardGenerator.generatePhysicalCardWithValues(m1);
	}

	@Override
	protected void tearDown() throws Exception {
		tempFile.delete();
		super.tearDown();
	}

	private void roundtrip() {
		try {
			store.save();
			store2 = CardCollectionStoreObject.initFromFile(tempFile);
			assertEquals(store.name, store2.name);
			assertEquals(store.list, store2.list);
			assertEquals(store.properties, store2.properties);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testbasicmc() {
		store.list.add(m1);
		roundtrip();
	}

	@Test
	public void testpropsmc() {
		MagicCard a1 = m1.cloneCard();
		MagicCard a2 = m1.cloneCard();
		this.store.list.add(a1);
		a2.setProperty(MagicCardField.PART, "test");
		this.store.list.add(a2);
		roundtrip();
	}

	public void testMcp() {
		store.list.add(mcp);
		roundtrip();
	}

	public void testMcp2() {
		store.list.add(mcp);
		store.list.add(CardGenerator.generatePhysicalCardWithValues(m1));
		roundtrip();
	}

	@Test
	public void testpropLegality() {
		MagicCard a1 = m1.cloneCard();
		LegalityMap map = LegalityMap.valueOf(Format.STANDARD);
		a1.setLegalityMap(map);
		this.store.list.add(a1);
		roundtrip();
		MagicCard a2 = (MagicCard) store2.list.get(0);
		LegalityMap map2 = a2.getLegalityMap();
		assertEquals(map, map2);
	}
}
