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
import java.util.List;

import com.reflexit.mtgtournament.core.model.Cube;
import com.reflexit.mtgtournament.core.model.PlayerList;
import com.reflexit.mtgtournament.core.model.Tournament;

public class TournamentManager {
	private static Cube root;

	public static synchronized Cube getCube() throws IOException, CoreException {
		if (root == null) {
			root = new Cube();
			PlayerList players = (PlayerList) loadFromFile("players.xml", new PlayerList());
			root.getPlayerList().addAllPlayers(players.getPlayers());
			IResource[] members = getProject().members();
			for (IResource resource : members) {
				if (resource.getFullPath().lastSegment().endsWith(".tour.xml")) {
					Tournament ts = (Tournament) loadFromFile(resource.getProjectRelativePath().lastSegment(),
					        new Tournament());
					ts.updateLinks(); // restore transient fields
					root.addTournament(ts);
				}
			}
		}
		return root;
	}

	public static void save() throws FileNotFoundException, CoreException {
		List<Tournament> tournamens = root.getTournamens();
		for (Object element : tournamens) {
			Tournament tournament = (Tournament) element;
			save(tournament);
		}
		save(root.getPlayerList());
	}

	public static void save(PlayerList list) throws CoreException, FileNotFoundException {
		String file = "players.xml";
		saveToFile(file, list);
	}

	public static void save(Tournament tournament) throws CoreException, FileNotFoundException {
		String file = tournament.getName() + ".tour.xml";
		saveToFile(file, tournament);
	}

	public static void saveToFile(String file, Object obj) throws CoreException, FileNotFoundException {
		IFile newFile = getProject().getFile(file);
		ModelLoader.save(obj, newFile.getLocation().toFile());
	}

	private static Object loadFromFile(String file, Object initObject) throws CoreException, FileNotFoundException,
	        IOException {
		IResource resource = getProject().findMember(file);
		if (resource == null || !resource.exists()) {
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
