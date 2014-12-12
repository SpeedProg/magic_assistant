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
package com.reflexit.magiccards.core.xml;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.utils.CardGenerator;
import com.reflexit.magiccards.core.xml.xstream.MagicCardPhysicalConvertor;
import com.reflexit.magiccards.core.xml.xstream.XstreamHandler;
import com.thoughtworks.xstream.XStream;

/**
 * TODO: add description
 */
public class MagicCardPhysicalConvertorTest extends TestCase {
	private XStream xstream;

	@Override
	protected void setUp() throws Exception {
		xstream = XstreamHandler.getXStream();
		xstream.registerConverter(new MagicCardPhysicalConvertor(xstream.getMapper(), xstream.getReflectionProvider()));
		xstream.setClassLoader(MagicCardPhysicalConvertorTest.class.getClassLoader());
	}

	public void testXStreamWriteNoDefaultFields() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setPrice(0);
		String xml = xstream.toXML(phi);
		assertTrue(!xml.contains("price"));
	}

	public void testXStreamWriteNoDefaultFieldsOwn() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setOwn(false);
		String xml = xstream.toXML(phi);
		assertTrue(!xml.contains("ownership"));
	}

	public void testXStreamWriteField() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setSpecialTag("fortrade");
		String xml = xstream.toXML(phi);
		assertTrue(xml.contains("<pfield>SPECIAL</pfield>"));
	}

	public void testXStreamAround() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();

		String xml = xstream.toXML(phi);
		Object object = xstream.fromXML(xml);
		assertEquals(phi, object);
		MagicCardPhysical p = (MagicCardPhysical) object;
		assert (p.getBase().getPhysicalCards().contains(p));
	}
}
