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

import org.eclipse.core.runtime.Path;
import org.junit.Test;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.DecksContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

/**
 * @author Alena
 *
 */
public class CardOrganizerTest extends TestCase {
	private ModelRoot root;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		this.root = DataManager.getModelRoot();
		this.root.reset();
	}

	/**
	 * Test method for {@link com.reflexit.magiccards.core.model.nav.CardOrganizer#findElement(org.eclipse.core.runtime.IPath)}.
	 */
	@Test
	public void testFindElement() {
		CardElement element = this.root.findElement(new Path("My Cards/Decks"));
		assertEquals(this.root.getDeckContainer(), element);
	}

	public void testFindElement2() {
		DecksContainer decks = this.root.getDeckContainer();
		DecksContainer con = decks.addDeckContainer("con");
		CardElement element = this.root.findElement(new Path("My Cards/Decks/con"));
		assertEquals(con, element);
	}

	/**
	 * Test method for {@link com.reflexit.magiccards.core.model.nav.CardElement#fireEvent(com.reflexit.magiccards.core.model.events.CardEvent)}.
	 */
	@Test
	public void testFireEvent() {
		final boolean res[] = new boolean[1];
		DecksContainer deckContainer = this.root.getDeckContainer();
		deckContainer.addListener(new ICardEventListener() {
			public void handleEvent(CardEvent event) {
				res[0] = true;
			}
		});
		deckContainer.addDeck("test");
		assertTrue("Event is not received", res[0]);
	}
}
