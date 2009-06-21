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
package com.reflexit.mtgtournament.core.xml;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.mtgtournament.core.model.Cube;
import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.Tournament;

public class TournamentManager {
	private static Cube root;

	public static synchronized Cube getCube() throws IOException, CoreException {
		if (root == null) {
			root = new Cube();
			Collection<Player> players = (Collection<Player>) loadFromFile("players.xml", new ArrayList<Player>());
			root.addAllPlayers(players);
			IResource[] members = getProject().members();
			for (IResource resource : members) {
				if (resource.getFullPath().lastSegment().endsWith(".tour.xml")) {
					Tournament ts = (Tournament) loadFromFile(resource.getFullPath().toOSString(), new Tournament());
					root.addTournament(ts);
				}
			}
		}
		return root;
	}

	private static Object loadFromFile(String file, Object initObject) throws CoreException, FileNotFoundException,
	        IOException {
		IResource resource = getProject().findMember(file);
		if (resource == null) {
			IFile newFile = getProject().getFile(file);
			ModelLoader.save(initObject, newFile.getLocation().toFile());
			return initObject;
		} else {
			return ModelLoader.load(resource.getLocation().toFile());
		}
	}

	public static IProject getProject() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject("tournaments");
		if (!project.exists())
			project.create(null);
		if (!project.isOpen())
			project.open(null);
		return project;
	}
}
