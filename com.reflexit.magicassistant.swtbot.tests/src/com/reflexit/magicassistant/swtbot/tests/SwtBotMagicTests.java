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
package com.reflexit.magicassistant.swtbot.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.reflexit.magiccards.ui.MagicUIActivator;

@RunWith(Suite.class)
@Suite.SuiteClasses({ //
		//
		FilterTest.class, //
		CreateDeck.class, //
		RenameDeck.class, //
		ImportDeckTest.class, //
		GroupByCost.class, //
		OverwriteDeckCheck.class, //
		EnterDeck.class,//
		AnalysersTest.class, //
		GalleryTest.class, //
})
public class SwtBotMagicTests {
	{
		System.setProperty("junit.testing", "true");
		MagicUIActivator.TRACE_TESTING = true;
	}
}
