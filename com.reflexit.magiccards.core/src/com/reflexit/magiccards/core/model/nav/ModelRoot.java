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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.Location;

/**
 * Model Root contains access to all deck, collections and magic db container
 * (displayed in the navigator)
 * 
 * @author Alena
 * 
 */
public class ModelRoot extends CardOrganizer {
	private static ModelRoot instance;
	private CollectionsContainer fDecks;
	private MagicDbContainter db;
	private CollectionsContainer fLib;
	private CardCollection fLibFile;
	private CollectionsContainer fMyCards;

	/**
	 * @param name
	 * @param parent2
	 */
	private ModelRoot() {
		super("Root", null);
		instance = this;
		initRoot();
	}

	private void initRoot() {
		try {
			CardOrganizer root = this;
			this.fMyCards = new CollectionsContainer("My Cards", root.getPath(), root);
			this.fLib = new CollectionsContainer("Collections", fMyCards);
			this.fDecks = new CollectionsContainer("Decks", fMyCards);
			this.db = new MagicDbContainter(root);
			this.fLibFile = new CardCollection("main.xml", this.fLib);
			refresh();
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	public void refresh() throws CoreException {
		this.fMyCards.loadChildren();
		// this.fLib.loadChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.nav.CardElement#getPath()
	 */
	@Override
	public IPath getPath() {
		return new Path("");
	}

	public CollectionsContainer getDeckContainer() {
		return this.fDecks;
	}

	public MagicDbContainter getMagicDBContainer() {
		return this.db;
	}

	public CollectionsContainer getCollectionsContainer() {
		return this.fLib;
	}

	public CollectionsContainer getMyCardsContainer() {
		return this.fMyCards;
	}

	/**
	 * @return
	 */
	public static synchronized ModelRoot getInstance() {
		if (instance == null)
			new ModelRoot();
		return instance;
	}

	/**
	 *
	 */
	public void reset() {
		getDeckContainer().removeChildren();
		getCollectionsContainer().removeChildren();
		this.fLibFile = new CardCollection("main.xml", this.fLib);
	}

	/**
	 * @return
	 */
	public CardCollection getDefaultLib() {
		return this.fLibFile;
	}

	/**
	 * @return map from string location to tree element
	 */
	public Map<Location, CardElement> getLocationsMap() {
		LinkedHashMap<Location, CardElement> map = new LinkedHashMap<Location, CardElement>();
		fillLocations(map, this);
		return map;
	}

	public CardElement getCardElement(Location location) {
		return getLocationsMap().get(location);
	}

	/**
	 * @param map
	 * @param modelRoot
	 */
	private void fillLocations(LinkedHashMap<Location, CardElement> map, CardElement root) {
		if (root instanceof CardCollection) {
			map.put(root.getLocation(), root);
		}
		if (root instanceof CardOrganizer) {
			CardOrganizer org = (CardOrganizer) root;
			if (org.getChildren() != null) {
				for (Object element : org.getChildren()) {
					CardElement el = (CardElement) element;
					fillLocations(map, el);
				}
			}
			map.put(root.getLocation(), root);
		}
	}

	public void move(CardElement[] elements, CardOrganizer newParent) {
		ArrayList<CardElement> norm = new ArrayList<CardElement>();
		list: for (CardElement el : elements) {
			for (CardElement no : norm) {
				if (el.getParent() == no)
					continue list;
			}
			norm.add(el);
		}
		for (CardElement no : norm) {
			no.newParent(newParent);
		}
		// System.err.println("drop to " + newParent);
	}
}
