package com.reflexit.magiccards.ui.dnd;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.unittesting.CardGenerator;

public class MagicCardTransferTest extends TestCase {
	private MagicCardTransfer trans;
	private MagicCard m1;
	private MagicCardPhysical mcp;
	private MagicCard m2;

	@Override
	@Before
	public void setUp() throws Exception {
		trans = MagicCardTransfer.getInstance();
		m1 = CardGenerator.generateCardWithValues();
		m2 = CardGenerator.generateCardWithValues();
		mcp = CardGenerator.generatePhysicalCardWithValues(m1);
		mcp.setLocation(Location.createLocation("mine"));
	}

	@Test
	public void testFromByteArrayRoundtrip() {
		IMagicCard[] cards = new IMagicCard[] { m1, m2 };
		byte[] byteArray = trans.toByteArray(cards);
		String str = new String(byteArray);
		IMagicCard[] res = trans.fromByteArray(byteArray);
		assertEquals(cards.length, res.length);
		assertEquals(cards[0], res[0]);
		assertEquals(cards[1], res[1]);
	}

	@Test
	public void testFromByteArrayRoundtripMcp() {
		MagicCardPhysical[] cards = new MagicCardPhysical[] { mcp };
		byte[] byteArray = trans.toByteArray(cards);
		String str = new String(byteArray);
		// System.err.println(str);
		IMagicCard[] res = trans.fromByteArray(byteArray);
		assertEquals(cards.length, res.length);
		assertEquals(cards[0], res[0]);
		assertEquals(cards[0].getLocation(), ((MagicCardPhysical) res[0]).getLocation());
	}

	@Test
	public void testFromByteArrayPair() {
		MagicCardPhysical mcp2 = (MagicCardPhysical) mcp.cloneCard();
		mcp2.setComment("mine");
		IMagicCard[] cards = new IMagicCard[] { mcp, mcp2 };
		byte[] byteArray = trans.toByteArray(cards);
		IMagicCard[] res = trans.fromByteArray(byteArray);
		assertEquals(cards.length, res.length);
		assertEquals(cards[0], res[0]);
		assertEquals(cards[1], res[1]);
	}
}
