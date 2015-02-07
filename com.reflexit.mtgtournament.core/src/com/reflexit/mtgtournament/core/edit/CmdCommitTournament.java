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
package com.reflexit.mtgtournament.core.edit;

import java.util.List;

import com.reflexit.mtgtournament.core.Activator;
import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.xml.TournamentManager;

/**
 * Command to add Round Table Scheduling into the round
 */
public class CmdCommitTournament implements ITCommand {
	private Tournament t;

	public CmdCommitTournament(Tournament t) {
		super();
		this.t = t;
	}

	public boolean execute() {
		if (t.isClosed())
			throw new IllegalStateException("Tournament is closed");
		List<PlayerTourInfo> playersInfo = t.getPlayersInfo();
		for (PlayerTourInfo pt : playersInfo) {
			Player player = pt.getPlayer();
			player.setPoints(player.getPoints() + pt.getPoints());
			player.setGames(player.getGames() + pt.getRoundsPlayed());
		}
		t.setClosed(true);
		try {
			TournamentManager.save(t);
			TournamentManager.save(TournamentManager.getCube().getPlayerList());
		} catch (Exception e) {
			Activator.log(e);
		}
		return true;
	}

	public boolean undo() {
		List<PlayerTourInfo> playersInfo = t.getPlayersInfo();
		for (PlayerTourInfo pt : playersInfo) {
			Player player = pt.getPlayer();
			player.setPoints(player.getPoints() - pt.getPoints());
			player.setGames(player.getGames() - pt.getRoundsPlayed());
		}
		t.setClosed(false);
		return true;
	}
}
