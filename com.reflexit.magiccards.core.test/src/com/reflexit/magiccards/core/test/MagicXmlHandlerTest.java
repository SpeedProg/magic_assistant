/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.test;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.utils.CardGenerator;
import com.reflexit.magiccards.core.xml.CardCollectionStoreObject;
import com.reflexit.magiccards.core.xml.MagicXmlStreamHandler;

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
		phi.setForTrade(0);
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
		phi.setForTrade(1);
		String xml = handler.toXML(phi);
		assertTrue(xml.contains("<forTrade>1</forTrade>"));
	}

	public void testXStreamAround() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setForTrade(1);
		String xml = handler.toXML(phi);
		CardCollectionStoreObject object = handler.fromXML(xml);
		Object o = object.list.get(0);
		assertEquals(phi, o);
		MagicCardPhysical p = (MagicCardPhysical) o;
		assert (p.getBase().getPhysicalCards().contains(p));
	}
}
