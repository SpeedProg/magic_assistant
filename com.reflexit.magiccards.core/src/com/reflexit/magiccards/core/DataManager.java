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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.thoughtworks.xstream.XStream;

public class DataManager {
	private static ICardHandler handler;
	private static ModelRoot root;

	public synchronized static ICardHandler getCardHandler() {
		if (handler != null)
			return handler;
		try {
			// String variant1 =
			// "com.reflexit.magiccards.core.sql.handlers.CardHolder";
			String variant2 = "com.reflexit.magiccards.core.xml.XmlCardHolder";
			@SuppressWarnings("rawtypes")
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

	public static synchronized ModelRoot getModelRoot() {
		if (root == null) {
			root = ModelRoot.getInstance();
		}
		return root;
	}

	public static IProject getProject() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsroot = workspace.getRoot();
		IProject project = wsroot.getProject("magiccards");
		if (!project.exists())
			project.create(null);
		if (!project.isOpen())
			project.open(null);
		return project;
	}

	public static XStream getXStream() {
		XStream xstream = new XStream();
		xstream.alias("mc", MagicCard.class);
		xstream.alias("mcp", MagicCardPhisical.class);
		return xstream;
	}
}
