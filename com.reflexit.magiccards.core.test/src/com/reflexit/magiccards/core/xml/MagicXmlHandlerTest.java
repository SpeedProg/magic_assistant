/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.xml;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.unittesting.CardGenerator;
import com.reflexit.unittesting.TestFileUtils;

/**
 * Test xml reader and writer
 */
public class MagicXmlHandlerTest extends TestCase {
	private MagicXmlStreamHandler handler;

	@Override
	protected void setUp() throws Exception {
		handler = new MagicXmlStreamHandler();
	}

	public void testXStreamWriteNoDefaultFields() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		// phi.setForTrade(0);
		String xml = handler.toXML(phi);
		assertTrue(!xml.contains("forTrade"));
	}

	public void testXStreamWriteNoDefaultFieldsOwn() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setOwn(false);
		String xml = handler.toXML(phi);
		assertTrue(!xml.contains("ownership"));
	}

	public void testXStreamWriteField() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		// phi.setForTrade(1);
		String xml = handler.toXML(phi);
		assertFalse(xml.contains("<forTrade>1</forTrade>"));
	}

	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}

	protected StringBuilder[] getContents(int sections) {
		try {
			return TestFileUtils.getContentsForTest("src", getClass(),
					getName(), sections);
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		}
	}

	/*-
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<cards>
	<name></name>
	<key></key>
	<comment></comment>
	<type></type>
	<list>
	<mcp>
	  <card>
	    <id>-37</id>
	    <name>name 37</name>
	    <edition>set 17</edition>
	  </card>
	  <count>1</count>
	  <comment>comment 38</comment>
	  <ownership>true</ownership>
	  <forTrade>1</forTrade>
	  <date>Thu Dec 04 07:39:54 EST 2014</date>
	</mcp>
	</list>
	</cards>
	 */
	public void testXStreamForTrade() {
		String xml = getAboveComment();
		CardCollectionStoreObject object = handler.fromXML(xml);
		Object o = object.list.get(0);
		MagicCardPhysical p = (MagicCardPhysical) o;
		assertEquals(1, p.getForTrade());
	}

	/*-
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<cards>
	<name></name>
	<key></key>
	<comment></comment>
	<type></type>
	<list>
	<mcp>
	  <card>
	    <id>-37</id>
	    <name>name 37</name>
	    <edition>set 17</edition>
	  </card>
	  <count>5</count>
	  <comment>comment 38</comment>
	  <ownership>true</ownership>
	  <forTrade>1</forTrade>
	  <date>Thu Dec 04 07:39:54 EST 2014</date>
	</mcp>
	</list>
	</cards>
	 */
	public void testXStreamForTrade2() {
		String xml = getAboveComment();
		CardCollectionStoreObject object = handler.fromXML(xml);
		MagicCardPhysical p = (MagicCardPhysical) object.list.get(0);
		assertEquals(1, p.getForTrade());
		assertEquals(5, p.getCount());
	}

	public void testXStreamAround1() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		String xml = handler.toXML(phi);
		CardCollectionStoreObject object = handler.fromXML(xml);
		Object o = object.list.get(0);
		assertEquals(phi, o);
		MagicCardPhysical p = (MagicCardPhysical) o;
	}

	public void testXStreamAround() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setCount(5);
		phi.setSpecialTag("foil");
		String xml = handler.toXML(phi);
		CardCollectionStoreObject object = handler.fromXML(xml);
		Object o = object.list.get(0);
		assertEquals(phi, o);
		MagicCardPhysical p = (MagicCardPhysical) o;
	}

	public void testXStreamAroundForTrade() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setCount(5);
		phi.set(MagicCardField.FORTRADECOUNT, 3);
		String xml = handler.toXML(phi);
		CardCollectionStoreObject object = handler.fromXML(xml);
		Object o = object.list.get(0);
		assertEquals(phi, o);
		MagicCardPhysical p = (MagicCardPhysical) o;
		assertEquals(3, p.getForTrade());
	}

	public void testNoMcFields() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setOwn(false);
		String xml = handler.toXML(phi);
		assertTrue("text is found in " + xml, !xml.contains("text"));
	}

	/*-
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<cards>
	  <name></name>
	  <key></key>
	  <comment></comment>
	  <type></type>
	  <list>
	    <mcp>
	      <card>
	        <id>-39</id>
	        <name>name 39</name>
	        <edition>set 39</edition>
	      </card>
	      <count>5</count>
	      <price>2.1</price>
	      <comment>comment 40</comment>
	      <custom>bla</custom>
	      <ownership>true</ownership>
	      <special>foil,c=mint</special>
	      <date>Sun Jan 11 22:37:54 EST 2015</date>
	    </mcp>
	  </list>
	</cards>
	 */
	public void testXmlGen() {
		String xml1 = getAboveComment().trim();
		MagicCardPhysical phi = new MagicCardPhysical(new MagicCard(), null);
		phi.getBase().setCardId(-39);
		phi.getBase().setName("name 39");
		phi.getBase().setSet("set 39");
		phi.setCount(5);
		phi.setSpecial("foil,c=mint");
		phi.setComment("comment 40");
		phi.setCustom("bla");
		phi.setOwn(true);
		phi.setPrice(2.1f);
		phi.setDate("Sun Jan 11 22:37:54 EST 2015");
		String xml = handler.toXML(phi);
		//System.err.println(xml);
		assertEquals(xml1, xml);
	}

	/*-
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<cards>
	<name>a</name>
	<key>a</key>
	<comment>text</comment>
	<type>deck</type>
	<list>
	<mcp>
	  <card>
	    <id>-39</id>
	    <name>name 39</name>
	    <edition>set 19</edition>
	  </card>
	  <count>5</count>
	  <price>2.1</price>
	  <comment>comment 40</comment>
	  <custom>bla</custom>
	  <ownership>true</ownership>
	  <special>foil,c=mint</special>
	  <date>Sun Jan 11 22:37:54 EST 2015</date>
	</mcp>
	</list>
	</cards>
	 */
	public void testXmlRead() {
		String xml = getAboveComment();
		CardCollectionStoreObject object = handler.fromXML(xml);
		assertEquals("a", object.name);
		assertEquals("a", object.key);
		assertEquals("text", object.comment);
		assertEquals("deck", object.type);
		MagicCardPhysical p = (MagicCardPhysical) object.list.get(0);
		assertEquals(-39, p.getCardId());
		assertEquals("name 39", p.getName());
		assertEquals("set 19", p.getSet());
		assertEquals(5, p.getCount());
		assertEquals(2.1f, p.getPrice());
		assertEquals("comment 40", p.getComment());
		assertEquals(true, p.isOwn());
		assertEquals("foil,c=mint", p.getSpecial());
		assertEquals("bla", p.getCustom());
		assertNotNull(p.getDate());
	}
/*-
  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cards>
  <name></name>
  <key></key>
  <comment></comment>
  <type></type>
  <list>
    <mc>
      <id>-39</id>
      <name>name 39</name>
      <cost>{4}</cost>
      <type>type 39</type>
      <power>4</power>
      <toughness>*</toughness>
      <oracleText>bla 39</oracleText>
      <edition>set 19</edition>
      <rarity>Common</rarity>
      <lang>Russian</lang>
      <rating>2.39</rating>
      <artist>Elena 39</artist>
      <num>39b</num>
      <text>bla &lt;br&gt; bla 39</text>
      <properties>
        <entry>
          <string>SIDE</string>
          <string>1</string>
        </entry>
        <entry>
          <string>LEGALITY</string>
          <string>Standard1|Modern</string>
        </entry>
      </properties>
    </mc>
  </list>
</cards>
 */
	public void testXmlGenMC() {
		String xml1 = getAboveComment().trim();
		MagicCard card = CardGenerator.genMagicCard(39);
		card.setCollNumber("39b");
		card.set(MagicCardField.SIDE, 1);
		Map<Format, Legality> map1 = new HashMap<>();
		map1.put(Format.STANDARD, Legality.RESTRICTED);
		map1.put(Format.MODERN, Legality.LEGAL);
		LegalityMap map = LegalityMap.valueOf(map1);
		card.setLegalityMap(map);
		String xml = handler.toXML(card);
		assertEquals(xml1, xml);
	}
	
	/*-
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<cards>
	<list>
	<mc>
	<id>27167</id>
	<name>Order // Chaos (Order)</name>
	<cost>{3}{W}</cost>
	<type>Instant</type>
	<oracleText>Exile target attacking creature.&lt;br&gt;</oracleText>
	<edition>Apocalypse</edition>
	<rarity>Uncommon</rarity>
	<rating>4.25</rating>
	<artist>Greg &amp; Tim Hildebrandt</artist>
	<num>132</num>
	<text>Exile target attacking creature.&lt;br&gt;</text>
	<properties>
	  <entry>
	    <string>PART</string>
	    <string>Order</string>
	  </entry>
	  <entry>
	    <string>OTHER_PART</string>
	    <string>Chaos</string>
	  </entry>
	  <entry>
	    <string>FLIPID</string>
	    <string>27168</string>
	  </entry>
	  <entry>
	    <string>NOUPDATE</string>
	    <string>true</string>
	  </entry>
	</properties>
	</mc>
	</list>
	</cards>
	 */
	public void testXmlReadParts() {
		String xml = getAboveComment();
		CardCollectionStoreObject object = handler.fromXML(xml);
		MagicCard p = (MagicCard) object.list.get(0);
		assertEquals(27167, p.getCardId());
		assertEquals(27168, p.getFlipId());
		assertEquals("Order", p.getBase().getPart());
		assertEquals("Chaos", p.getBase().getProperty(MagicCardField.OTHER_PART));
		assertEquals(132, p.getCollectorNumberId());
		assertEquals("Uncommon", p.getRarity());
		assertEquals(4.25f, p.getRating());
		assertEquals(true, p.getProperty(MagicCardField.NOUPDATE));
	}

	/*-
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<cards>
	  <name></name>
	  <key></key>
	  <comment></comment>
	  <type></type>
	  <list>
	    <mc>
	      <id>-39</id>
	      <name>name 39</name>
	      <cost>{4}</cost>
	      <type>type 39</type>
	      <power>4</power>
	      <toughness>*</toughness>
	      <oracleText>bla 39</oracleText>
	      <edition>set 19</edition>
	      <rarity>Common</rarity>
	      <lang>Russian</lang>
	      <rating>2.39</rating>
	      <artist>Elena 39</artist>
	      <num>39b</num>
	      <text>bla &lt;br&gt; bla 39</text>
	      <properties>
	        <entry>
	          <string>SIDE</string>
	          <string>1</string>
	        </entry>
	        <entry>
	          <string>LEGALITY</string>
	          <string>Standard1|Modern</string>
	        </entry>
	      </properties>
	    </mc>
	  </list>
	</cards>
	 */
	public void testXmlReadLegality() {
		String xml = getAboveComment();
		CardCollectionStoreObject object = handler.fromXML(xml);
		MagicCard p = (MagicCard) object.list.get(0);
		assertEquals(1, p.getSide());
		LegalityMap lm = p.getLegalityMap();
		assertTrue(lm.isLegal(Format.MODERN));
		assertEquals(Legality.RESTRICTED, lm.get(Format.STANDARD));
	}
}
