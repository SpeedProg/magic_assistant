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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.reflexit.magiccards.core.exports.DeckParserTest;
import com.reflexit.magiccards.core.exports.ImportUtilsTest;
import com.reflexit.magiccards.core.exports.MTGStudioImportTest;
import com.reflexit.magiccards.core.exports.MagicWorkstationImportTest;
import com.reflexit.magiccards.core.exports.ManaDeckImportTest;
import com.reflexit.magiccards.core.exports.MtgoImportTest;
import com.reflexit.magiccards.core.exports.ShandalarImportTest;
import com.reflexit.magiccards.core.exports.TablePipedImportTest;
import com.reflexit.magiccards.core.model.CardGroupTest;
import com.reflexit.magiccards.core.model.SortOrderTest;
import com.reflexit.magiccards.core.model.nav.CardElementTest;

/**
 * @author Alena
 * 
 */
public class AllLocalTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.reflexit.magiccards.core.test.local");
		// $JUnit-BEGIN$
		// import
		suite.addTestSuite(TablePipedImportTest.class);
		suite.addTestSuite(MtgoImportTest.class);
		suite.addTestSuite(MagicWorkstationImportTest.class);
		suite.addTestSuite(DeckParserTest.class);
		suite.addTestSuite(ImportUtilsTest.class);
		suite.addTestSuite(ManaDeckImportTest.class);
		suite.addTestSuite(ShandalarImportTest.class);
		suite.addTestSuite(MTGStudioImportTest.class);
		// core
		suite.addTestSuite(DbMultiFileCardStoreTest.class);
		suite.addTestSuite(MultiFileCollectionStoreTest.class);
		suite.addTestSuite(CardOrganizerTest.class);
		suite.addTestSuite(AbstractFilteredCardStoreTest.class);
		suite.addTestSuite(DeckStoreTest.class);
		suite.addTestSuite(CardCollectionStoreObjectTest.class);
		suite.addTestSuite(CollectionStoreTest.class);
		suite.addTestSuite(MagicCardFilterTest.class);
		suite.addTestSuite(MagicCardPhysicalConvertorTest.class);
		// suite.addTestSuite(CardTextNL1Test.class); TODO
		suite.addTestSuite(CardElementTest.class);
		suite.addTestSuite(CardGroupTest.class);
		suite.addTestSuite(SortOrderTest.class);
		// $JUnit-END$
		return suite;
	}
}
