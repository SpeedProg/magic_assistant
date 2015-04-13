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
package com.reflexit.magiccards.core.seller.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Alena
 *
 */
public class AllSellerTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.reflexit.magiccards.core.seller.test");
		// $JUnit-BEGIN$
		// prive providers
		suite.addTestSuite(ParseTcgPlayerPricesTest.class);
		suite.addTestSuite(ParseMOTLPricesTest.class);
		suite.addTestSuite(ParseMagicCardMarketPricesTest.class);
		// $JUnit-END$
		return suite;
	}
}
