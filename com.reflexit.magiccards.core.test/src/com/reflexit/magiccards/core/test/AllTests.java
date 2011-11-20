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
import com.reflexit.magiccards.core.exports.MagicWorkstationImportTest;
import com.reflexit.magiccards.core.exports.MtgoImportTest;
import com.reflexit.magiccards.core.exports.TablePipedImportTest;
import com.reflexit.magiccards.core.model.nav.CardElementTest;
import com.reflexit.magiccards.core.sync.ParseGathererBasicInfoTest;
import com.reflexit.magiccards.core.sync.ParseGathererCardLanguagesTest;
import com.reflexit.magiccards.core.sync.ParseGathererLegalityTest;
import com.reflexit.magiccards.core.sync.ParseGathererRulingsTest;
import com.reflexit.magiccards.core.sync.ParseGathererSetsTest;

/**
 * @author Alena
 * 
 */
public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.reflexit.magiccards.core.test");
		// $JUnit-BEGIN$
		// import
		suite.addTestSuite(TablePipedImportTest.class);
		suite.addTestSuite(MtgoImportTest.class);
		suite.addTestSuite(MagicWorkstationImportTest.class);
		suite.addTestSuite(DeckParserTest.class);
		// core
		suite.addTestSuite(VirtualMultiFileCardStoreTest.class);
		suite.addTestSuite(MultiFileCollectionStoreTest.class);
		suite.addTestSuite(CardOrganizerTest.class);
		suite.addTestSuite(MemCardHandlerTest.class);
		suite.addTestSuite(DeckStoreTest.class);
		suite.addTestSuite(CollectionStoreTest.class);
		suite.addTestSuite(MagicCardFilterTest.class);
		suite.addTestSuite(MagicCardPhysicalConvertorTest.class);
		suite.addTestSuite(CardTextNL1Test.class);
		suite.addTestSuite(CardElementTest.class);
		// gatherer
		suite.addTestSuite(ParseGathererCardLanguagesTest.class);
		suite.addTestSuite(ParseGathererSetsTest.class);
		suite.addTestSuite(ParseGathererLegalityTest.class);
		suite.addTestSuite(ParseGathererRulingsTest.class);
		suite.addTestSuite(ParseGathererBasicInfoTest.class);
		// $JUnit-END$
		return suite;
	}
}
