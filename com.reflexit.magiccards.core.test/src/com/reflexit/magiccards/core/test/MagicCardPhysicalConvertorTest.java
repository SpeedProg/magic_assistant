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

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.test.assist.CardGenerator;
import com.reflexit.magiccards.core.xml.xstream.MagicCardPhysicalConvertor;
import com.thoughtworks.xstream.XStream;

/**
 * TODO: add description
 */
public class MagicCardPhysicalConvertorTest extends TestCase {
	private XStream xstream;

	@Override
	protected void setUp() throws Exception {
		xstream = DataManager.getXStream();
		xstream.registerConverter(new MagicCardPhysicalConvertor(xstream.getMapper(), xstream.getReflectionProvider()));
		xstream.setClassLoader(MagicCardPhysicalConvertorTest.class.getClassLoader());
	}

	public void testXStreamWriteNoDefaultFields() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setForTrade(0);
		String xml = xstream.toXML(phi);
		assertTrue(!xml.contains("forTrade"));
	}

	public void testXStreamWriteNoDefaultFieldsOwn() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setOwn(false);
		String xml = xstream.toXML(phi);
		assertTrue(!xml.contains("ownership"));
	}

	public void testXStreamWriteField() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setForTrade(1);
		String xml = xstream.toXML(phi);
		assertTrue(xml.contains("<forTrade>1"));
	}

	public void testXStreamAround() {
		MagicCardPhysical phi = CardGenerator.generatePhysicalCardWithValues();
		phi.setForTrade(1);
		String xml = xstream.toXML(phi);
		Object object = xstream.fromXML(xml);
		assertEquals(phi, object);
	}
}
