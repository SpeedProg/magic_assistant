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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.util.LinkedHashMap;
import java.util.Map;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;

/**
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
			this.fLibFile.setVirtual(false);
			refresh();
			convertData();
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	public void refresh() throws CoreException {
		this.fMyCards.loadChildren();
		//this.fLib.loadChildren();
	}

	/**
	 * 
	 */
	private void convertData() {
		// move library data of 1.0.2 into Collections dir
		try {
			IPath newloc = this.fLibFile.getPath();
			IResource main = this.fLibFile.getResource();
			IResource lib = DataManager.getProject().findMember("library.xml");
			if (lib != null && lib.exists()) {
				if (main == null || !main.exists())
					lib.move(newloc, true, null);
				else if (main.getLocation().toFile().length() == 0) {
					main.delete(true, null);
					lib.move(newloc, true, null);
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	/* (non-Javadoc)
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
	public static ModelRoot getInstance() {
		if (instance == null)
			instance = new ModelRoot();
		return instance;
	}

	/**
	 * 
	 */
	public void reset() {
		getDeckContainer().removeChildren();
		getCollectionsContainer().removeChildren();
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
	public Map getLocationsMap() {
		LinkedHashMap<Location, CardElement> map = new LinkedHashMap<Location, CardElement>();
		fillLocations(map, this);
		return map;
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
			if (org.getChildren() == null)
				return;
			for (Object element : org.getChildren()) {
				CardElement el = (CardElement) element;
				fillLocations(map, el);
			}
		}
	}
}
