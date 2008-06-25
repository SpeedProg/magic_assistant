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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;

/**
 * @author Alena
 *
 */
public class LibraryRoot extends CardOrganizer {
	private CardOrganizer fDecks;
	private CardOrganizer db;

	/**
	 * @param name
	 * @param parent2
	 */
	public LibraryRoot() {
		super("Root", null);
		initRoot();
	}

	public Deck addDeck(String name) {
		Deck d = new Deck(name, this.fDecks);
		this.fDecks.addChild(d);
		return d;
	}

	private void initRoot() {
		try {
			IProject project = DataManager.getProject();
			CardOrganizer root = this;
			root.addChild(new Library());
			IFolder decks = project.getFolder("Decks");
			if (!decks.exists())
				decks.create(IResource.NONE, true, null);
			this.fDecks = new CardOrganizer("Decks", root);
			root.addChild(this.fDecks);
			load(decks, this.fDecks);
			this.db = new CardOrganizer("All Cards", root);
			root.addChild(this.db);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void load(IContainer res, CardOrganizer parent) throws CoreException {
		IResource[] members = res.members();
		for (int i = 0; i < members.length; i++) {
			IResource mem = members[i];
			String name = mem.getName();
			if (mem instanceof IContainer) {
				CardOrganizer con = new CardOrganizer(name, parent);
				parent.addChild(con);
				load((IContainer) mem, con);
			} else if (mem != null) {
				if (name.endsWith(".xml")) {
					CardElement con = new Deck(name, parent);
					parent.addChild(con);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.nav.CardElement#getPath()
	 */
	@Override
	public IPath getPath() {
		return new Path("");
	}

	/**
	 * @param id
	 * @return
	 */
	public Deck getDeck(String id) {
		for (Iterator iterator = this.fDecks.getChildren().iterator(); iterator.hasNext();) {
			Deck d = (Deck) iterator.next();
			if (d.getFileName().equals(id))
				return d;
		}
		return null;
	}

	/**
	 * @param el
	 */
	public void removeDeck(Deck el) {
		this.fDecks.removeChild(el);
	}
}
