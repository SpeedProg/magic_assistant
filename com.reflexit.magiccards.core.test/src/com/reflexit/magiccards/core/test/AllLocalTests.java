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

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.reflexit.magiccards.core.DataManagerTest;
import com.reflexit.magiccards.core.FileUtilsTest;
import com.reflexit.magiccards.core.exports.ExportImportSuite;
import com.reflexit.magiccards.core.model.AbilitiesTest;
import com.reflexit.magiccards.core.model.CardGroupTest;
import com.reflexit.magiccards.core.model.ColorsTest;
import com.reflexit.magiccards.core.model.EditionsTest;
import com.reflexit.magiccards.core.model.GrouppingPerformanceTest;
import com.reflexit.magiccards.core.model.LegalityMapTest;
import com.reflexit.magiccards.core.model.MagicCardComparatorTest;
import com.reflexit.magiccards.core.model.MagicCardFilterTest;
import com.reflexit.magiccards.core.model.MagicCardListTest;
import com.reflexit.magiccards.core.model.MagicCardTest;
import com.reflexit.magiccards.core.model.MagicCardTest_failure_1;
import com.reflexit.magiccards.core.model.MagicCardTest_failure_2;
import com.reflexit.magiccards.core.model.PlayingDeckTest;
import com.reflexit.magiccards.core.model.SortOrderTest;
import com.reflexit.magiccards.core.model.nav.CardElementTest;
import com.reflexit.magiccards.core.model.nav.CardOrganizerTest;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStoreTest;
import com.reflexit.magiccards.core.model.storage.CardCollectionStoreObjectTest;
import com.reflexit.magiccards.core.model.storage.CollectionStoreTest;
import com.reflexit.magiccards.core.model.storage.DbMultiFileCardStoreTest;
import com.reflexit.magiccards.core.model.storage.DeckStoreTest;
import com.reflexit.magiccards.core.model.storage.MultiFileCollectionStoreTest;
import com.reflexit.magiccards.core.model.utils.CardStoreUtilsTest;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizerTest;
import com.reflexit.magiccards.core.xml.MagicXmlHandlerTest;

/**
 * @author Alena
 *
 */
@RunWith(AllTests.class)
public class AllLocalTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.reflexit.magiccards.core.test.local");
		// $JUnit-BEGIN$
		// import
		suite.addTest(new JUnit4TestAdapter(ExportImportSuite.class));
		// core
		suite.addTestSuite(MagicCardTest.class);
		suite.addTestSuite(MagicCardTest_failure_1.class);
		suite.addTestSuite(MagicCardTest_failure_2.class);
		suite.addTestSuite(DbMultiFileCardStoreTest.class);
		suite.addTestSuite(MultiFileCollectionStoreTest.class);
		suite.addTestSuite(CardOrganizerTest.class);
		suite.addTestSuite(AbstractFilteredCardStoreTest.class);
		suite.addTestSuite(DeckStoreTest.class);
		suite.addTestSuite(CardCollectionStoreObjectTest.class);
		suite.addTestSuite(CollectionStoreTest.class);
		suite.addTestSuite(MagicCardFilterTest.class);
		suite.addTestSuite(MagicXmlHandlerTest.class);
		suite.addTestSuite(DataManagerTest.class);
		suite.addTestSuite(FileUtilsTest.class);
		// suite.addTestSuite(CardTextNL1Test.class); TODO
		suite.addTestSuite(CardElementTest.class);
		suite.addTestSuite(CardGroupTest.class);
		suite.addTest(new JUnit4TestAdapter(ColorsTest.class));
		suite.addTestSuite(SortOrderTest.class);
		suite.addTestSuite(SearchStringTokenizerTest.class);
		suite.addTest(new JUnit4TestAdapter(CardStoreUtilsTest.class));
		suite.addTestSuite(MagicCardComparatorTest.class);
		suite.addTestSuite(LegalityMapTest.class);
		suite.addTestSuite(AbilitiesTest.class);
		suite.addTest(new JUnit4TestAdapter(MagicCardListTest.class));
		suite.addTest(new JUnit4TestAdapter(PlayingDeckTest.class));
		// editions
		suite.addTestSuite(EditionsTest.class);
		// Perf
		suite.addTestSuite(GrouppingPerformanceTest.class);
		// $JUnit-END$
		return suite;
	}
}
