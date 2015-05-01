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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Location;

/**
 * Model Root contains access to all deck, collections and magic db container
 * (displayed in the navigator)
 *
 * @author Alena
 *
 */
public class ModelRoot extends CardOrganizer {
	private CollectionsContainer fDecks;
	private MagicDbContainter db;
	private CollectionsContainer fLib;
	private CardCollection fLibFile;
	private CollectionsContainer fMyCards;
	private File rootDir;

	/**
	 * @param name
	 * @param parent2
	 */
	private ModelRoot(File dir) {
		super("Root", null);
		resetRoot(dir);
	}

	public void resetRoot(File dir) {
		CardOrganizer root = this;
		rootDir = dir;
		removeChildren();
		this.fMyCards = new CollectionsContainer(root.getPath(), root) {
			@Override
			public String getLabel() {
				return "My Cards";
			}
		};
		this.fLib = new CollectionsContainer("Collections", fMyCards);
		this.fDecks = new CollectionsContainer("Decks", fMyCards);
		this.db = new MagicDbContainter(root);
		this.fLibFile = new CardCollection("main.xml", this.fLib);
		refresh();
	}

	public void refresh() {
		this.fMyCards.loadChildren();
		// this.fLib.loadChildren();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.core.model.nav.CardElement#getPath()
	 */
	@Override
	public LocationPath getPath() {
		return LocationPath.ROOT;
	}

	@Override
	public boolean isRoot() {
		return true;
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
	public static ModelRoot getInstance(File dir) {
		return new ModelRoot(dir);
	}

	/**
	 * @param temp
	 *
	 */
	public void clear() {
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
		if (newParent instanceof MagicDbContainter)
			throw new MagicException("Cannot move to db");
		if (newParent == getMyCardsContainer())
			throw new MagicException("Cannot move into My Cards folder");
		ArrayList<CardElement> norm = new ArrayList<CardElement>();
		for (CardElement el : elements) {
			if (el == null)
				continue;
			if (norm.contains(el.getParent()))
				continue;
			if (norm.contains(el.getRelated()))
				continue;
			norm.add(el);
		}
		for (CardElement no : norm) {
			CardElement related = no.getRelated();
			no.newParent(newParent);
			if (related != null)
				related.newParent(newParent);
		}
		// System.err.println("drop to " + newParent);
	}

	@Override
	public File getRootDir() {
		if (rootDir == null)
			throw new NullPointerException();
		return rootDir;
	}
}
