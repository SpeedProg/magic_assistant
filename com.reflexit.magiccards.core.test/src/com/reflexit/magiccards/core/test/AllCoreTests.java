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

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.reflexit.magiccards.core.exports.MagicCardsInfoImportTest;
import com.reflexit.magiccards.core.model.storage.PerformanceFilteringTest;
import com.reflexit.magiccards.core.seller.test.AllSellerTests;
import com.reflexit.magiccards.core.sync.CurrencyConvertorTest;
import com.reflexit.magiccards.core.sync.ParseGathererCardLanguagesTest;
import com.reflexit.magiccards.core.sync.ParseGathererLegalityTest;
import com.reflexit.magiccards.core.sync.ParseGathererOracleTest;
import com.reflexit.magiccards.core.sync.ParseGathererPrintedTest;
import com.reflexit.magiccards.core.sync.ParseGathererSearchChecklistTest;
import com.reflexit.magiccards.core.sync.ParseGathererSearchStandardTest;
import com.reflexit.magiccards.core.sync.ParseGathererSetsTest;

/**
 * @author Alena
 *
 */
public class AllCoreTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.reflexit.magiccards.core.test");
		// $JUnit-BEGIN$
		suite.addTest(AllLocalTests.suite());
		// gatherer
		suite.addTestSuite(ParseGathererCardLanguagesTest.class);
		suite.addTestSuite(ParseGathererSetsTest.class);
		suite.addTestSuite(ParseGathererLegalityTest.class);
		suite.addTestSuite(ParseGathererOracleTest.class);
		suite.addTestSuite(ParseGathererPrintedTest.class);
		suite.addTestSuite(ParseGathererSearchStandardTest.class);
		suite.addTestSuite(ParseGathererSearchChecklistTest.class);
		suite.addTestSuite(CurrencyConvertorTest.class);
		suite.addTestSuite(PerformanceFilteringTest.class);
		// online import
		suite.addTest(new JUnit4TestAdapter(MagicCardsInfoImportTest.class));
		// price providers
		suite.addTest(AllSellerTests.suite());
		// $JUnit-END$
		return suite;
	}
}
