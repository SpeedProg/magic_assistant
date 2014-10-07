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
package com.reflexit.magiccards.core.model.nav;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.test.assist.TestFileUtils;

/**
 * @author Alena
 * 
 */
public class CardOrganizerTest extends TestCase {
	private ModelRoot root;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		TestFileUtils.resetDb();
		root = DataManager.getInstance().getModelRoot();
	}

	/**
	 * Test method for {@link com.reflexit.magiccards.core.model.nav.CardOrganizer#findElement(org.eclipse.core.runtime.IPath)} .
	 */
	@Test
	public void testFindElement() {
		CardElement element = this.root.findElement(new LocationPath("My Cards/Decks"));
		assertEquals(this.root.getDeckContainer(), element);
	}

	public void testFindElement2() {
		CollectionsContainer decks = this.root.getDeckContainer();
		CollectionsContainer con = decks.addCollectionsContainer("cox");
		CardElement element = this.root.findElement(new LocationPath("My Cards/Decks/cox"));
		assertEquals(con, element);
	}

	/**
	 * Test method for
	 * {@link com.reflexit.magiccards.core.model.nav.CardElement#fireEvent(com.reflexit.magiccards.core.model.events.CardEvent)} .
	 */
	@Test
	public void testFireEvent() {
		final boolean res[] = new boolean[1];
		CollectionsContainer deckContainer = this.root.getDeckContainer();
		deckContainer.addListener(new ICardEventListener() {
			@Override
			public void handleEvent(CardEvent event) {
				res[0] = true;
			}
		});
		deckContainer.addDeck("test");
		assertTrue("Event is not received", res[0]);
	}
}
