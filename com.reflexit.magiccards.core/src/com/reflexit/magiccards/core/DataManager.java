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
package com.reflexit.magiccards.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.nav.LibraryRoot;
import com.thoughtworks.xstream.XStream;

public class DataManager {
	static ICardHandler handler;
	private static LibraryRoot root;

	public static ICardHandler getCardHandler() {
		if (handler != null)
			return handler;
		try {
			String variant1 = "com.reflexit.magiccards.core.sql.handlers.CardHolder";
			String variant2 = "com.reflexit.magiccards.core.xml.XmlCardHolder";
			Class c = Class.forName(variant2);
			Object x = c.newInstance();
			handler = (ICardHandler) x;
			return handler;
		} catch (InstantiationException e) {
			Activator.log(e);
			return null;
		} catch (IllegalAccessException e) {
			Activator.log(e);
			return null;
		} catch (ClassNotFoundException e) {
			Activator.log(e);
			return null;
		}
	}

	public static LibraryRoot getModelRoot() {
		if (root == null) {
			root = new LibraryRoot();
		}
		return root;
	}

	public static IProject getProject() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject("magiccards");
		if (!project.exists())
			project.create(null);
		if (!project.isOpen())
			project.open(null);
		return project;
	}

	public static IFolder getDbFolder() throws CoreException {
		IFolder dir = getProject().getFolder("MagicDB");
		if (!dir.exists())
			dir.create(IResource.NONE, true, null);
		return dir;
	}

	public static XStream getXStream() {
    	XStream xstream = new XStream();
    	xstream.alias("mc", MagicCard.class);
    	xstream.alias("mcp", MagicCardPhisical.class);
    	return xstream;
    }
}
