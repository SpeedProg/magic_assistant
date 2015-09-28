/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ShandalarImportTest extends AbstarctImportTest {
	/*-
	;Azaar - Lichlord
	;Black
	;Coyote Tex
	;Coyote Tex@AOL.Com
	;September 19, 1996
	;7
	;4th Edition
	;comments

	.239	22	Swamp
	.55	4	Dark Ritual
	.230	1	Sol Ring
	.166	1	Mox Jet
	.17	1	Black Lotus
	.18	1	Black Vise
	.162	1	Mind Twist
	.68	4	Drain Life
	.117	4	Hypnotic Specter
	.220	4	Sengir Vampire
	.534	3	Tetravus
	.151	2	Lord of the Pit
	.661	2	Greed
	.286	3	Will-O'-The-Wisp
	.100	1	Gloom
	.336	3	Bog Imp
	.113	4	Howl from Beyond
	 */
	private ShandalarDeckDckImportDelegate mimport = new ShandalarDeckDckImportDelegate();
	static {
		DataManager.getCardHandler().getMagicDBStore().initialize();
	}

	void ppd(int i) {
		if (i == 0)
			preview(mimport);
		else
			parse(mimport);
	}

	@Test
	public void test1() {
		addLine(";Azaar - Lichlord\n" + //
				";Black\n" + //
				";Coyote Tex\n" + //
				";Coyote Tex@AOL.Com\n" + //
				";September 19, 1996\n" + //
				";7\n" + //
				";4th Edition");//
		addLine(".239	22	Swamp");
		addLine(".55	4	Dark Ritual");
		addLine(".166	1	Mox Jet");
		addLine(".17	1	Black Lotus");
		for (int i = 0; i < 2; i++) {
			ppd(i);
			assertEquals(4, resSize);
			assertEquals("Swamp", card1.getName());
			assertEquals(22, ((MagicCardPhysical) card1).getCount());
			assertNull(((MagicCardPhysical) card1).getError());
			assertEquals("Fourth Edition", card2.getSet());
			assertNull(((MagicCardPhysical) card2).getError());
			assertNotNull(((MagicCardPhysical) card3).getError());
			assertNotNull(((MagicCardPhysical) cardN).getError());
		}
	}
}
