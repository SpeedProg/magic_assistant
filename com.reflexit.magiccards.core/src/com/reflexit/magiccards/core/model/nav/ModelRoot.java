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

import java.util.Iterator;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;

/**
 * @author Alena
 *
 */
public class ModelRoot extends CardOrganizer {
	private static ModelRoot instance;
	private DecksContainer fDecks;
	private MagicDbContainter db;
	private CollectionsContainer fLib;

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
			this.fLib = new CollectionsContainer("Collections", root);
			this.fDecks = new DecksContainer("Decks", root);
			this.db = new MagicDbContainter(root);
			convertData();
			this.fDecks.loadChildren();
			this.fLib.loadChildren();
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	/**
	 * 
	 */
	private void convertData() {
		// initialize default dirs
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();) {
			CardElement element = (CardElement) iterator.next();
			if (element instanceof CardOrganizer) {
				try {
					((CardOrganizer) element).create();
				} catch (CoreException e) {
					Activator.log(e);
				}
			}
		}
		// move library data of 1.0.2 into Collections dir
		try {
			IResource lib = DataManager.getProject().findMember("library.xml");
			if (lib != null && lib.exists()) {
				IPath newloc = this.fLib.getPath().append("main.xml");
				IResource main = this.fLib.getContainer().findMember(newloc.lastSegment().toString());
				if (main == null || !main.exists())
					lib.move(newloc, true, null);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	public Deck getDeck(String id) {
		return getDeckContainer().findDeck(id);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.nav.CardElement#getPath()
	 */
	@Override
	public IPath getPath() {
		return new Path("");
	}

	public DecksContainer getDeckContainer() {
		return this.fDecks;
	}

	public MagicDbContainter getMagicDBContainer() {
		return this.db;
	}

	public CollectionsContainer getCollectionsContainer() {
		return this.fLib;
	}

	/**
	 * @return
	 */
	public static ModelRoot getInstance() {
		if (instance == null)
			instance = new ModelRoot();
		return instance;
	}
}
