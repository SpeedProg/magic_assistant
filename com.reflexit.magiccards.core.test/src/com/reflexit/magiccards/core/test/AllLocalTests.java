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

import com.reflexit.magiccards.core.exports.ClassicExportDelegateTest;
import com.reflexit.magiccards.core.exports.ClassicImportDelegateTest;
import com.reflexit.magiccards.core.exports.CsvExportDelegateTest;
import com.reflexit.magiccards.core.exports.CustomExportDelegateTest;
import com.reflexit.magiccards.core.exports.DeckParserTest;
import com.reflexit.magiccards.core.exports.ImportUtilsTest;
import com.reflexit.magiccards.core.exports.MTGStudioImportTest;
import com.reflexit.magiccards.core.exports.MagicWorkstationImportTest;
import com.reflexit.magiccards.core.exports.ManaDeckImportTest;
import com.reflexit.magiccards.core.exports.MtgoImportTest;
import com.reflexit.magiccards.core.exports.PipedTableExportText;
import com.reflexit.magiccards.core.exports.ShandalarImportTest;
import com.reflexit.magiccards.core.exports.TablePipedImportTest;
import com.reflexit.magiccards.core.model.CardGroupTest;
import com.reflexit.magiccards.core.model.GrouppingPerformanceTest;
import com.reflexit.magiccards.core.model.LegalityMapTest;
import com.reflexit.magiccards.core.model.MagicCardComparatorTest;
import com.reflexit.magiccards.core.model.MagicCardFilterTest;
import com.reflexit.magiccards.core.model.SortOrderTest;
import com.reflexit.magiccards.core.model.nav.CardElementTest;
import com.reflexit.magiccards.core.model.nav.CardOrganizerTest;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStoreTest;
import com.reflexit.magiccards.core.model.storage.CardCollectionStoreObjectTest;
import com.reflexit.magiccards.core.model.storage.CollectionStoreTest;
import com.reflexit.magiccards.core.model.storage.DbMultiFileCardStoreTest;
import com.reflexit.magiccards.core.model.storage.DeckStoreTest;
import com.reflexit.magiccards.core.model.storage.MultiFileCollectionStoreTest;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizerTest;
import com.reflexit.magiccards.core.xml.MagicCardPhysicalConvertorTest;
import com.reflexit.magiccards.core.xml.MagicXmlHandlerTest;

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
		suite.addTestSuite(PipedTableExportText.class);
		suite.addTestSuite(CsvExportDelegateTest.class);
		suite.addTestSuite(ClassicExportDelegateTest.class);
		suite.addTestSuite(ClassicImportDelegateTest.class);
		suite.addTestSuite(CustomExportDelegateTest.class);
		// core
		suite.addTestSuite(DbMultiFileCardStoreTest.class);
		suite.addTestSuite(MultiFileCollectionStoreTest.class);
		suite.addTestSuite(CardOrganizerTest.class);
		suite.addTestSuite(AbstractFilteredCardStoreTest.class);
		suite.addTestSuite(DeckStoreTest.class);
		suite.addTestSuite(CardCollectionStoreObjectTest.class);
		suite.addTestSuite(CollectionStoreTest.class);
		suite.addTestSuite(MagicCardFilterTest.class);
		suite.addTestSuite(MagicXmlHandlerTest.class);
		suite.addTestSuite(MagicCardPhysicalConvertorTest.class);
		// suite.addTestSuite(CardTextNL1Test.class); TODO
		suite.addTestSuite(CardElementTest.class);
		suite.addTestSuite(CardGroupTest.class);
		suite.addTestSuite(SortOrderTest.class);
		suite.addTestSuite(SearchStringTokenizerTest.class);
		suite.addTestSuite(MagicCardComparatorTest.class);
		suite.addTestSuite(LegalityMapTest.class);
		// Perf
		suite.addTestSuite(GrouppingPerformanceTest.class);
		// $JUnit-END$
		return suite;
	}
}
