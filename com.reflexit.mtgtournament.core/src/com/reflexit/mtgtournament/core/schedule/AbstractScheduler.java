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
package com.reflexit.mtgtournament.core.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.RoundState;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
 * @author Alena
 * 
 */
public abstract class AbstractScheduler implements IScheduler {
	public void schedule(Tournament t) {
		updateRoundNumber(t);
		createDraft(t);
		for (int i = 1; i <= t.getNumberOfRounds(); i++) {
			Round r = new Round(i);
			t.addRound(r);
			r.setType(t.getType());
		}
	}

	protected void createDraft(Tournament t) {
		Round r = new Round(0);
		t.addRound(r);
		r.setType(TournamentType.RANDOM);
		if (!t.hasDraftRound()) {
			// draft always there, but not visible sometimes
			r.schedule();
			r.close();
		}
	}

	protected void updateRoundNumber(Tournament t) {
		int x = t.getNumberOfRounds();
		if (x == 0) {
			int p = t.getNumberOfPlayers();
			x = p - 1;
			if (x < 1)
				x = 1;
			t.setNumberOfRounds(x);
		}
	}

	public void schedule(Round r) {
		checkType(r);
		if (r.getState() == RoundState.NOT_READY) {
			throw new IllegalStateException("Round is not ready");
		}
		if (r.getState() != RoundState.NOT_SCHEDULED) {
			throw new IllegalStateException("Round is already scheduled");
		}
		// System.err.println("Round ======" + r.getNumber());
		// get active players and sort them by place
		ArrayList<PlayerTourInfo> players = new ArrayList<PlayerTourInfo>(r.getTournament().getPlayersInfo());
		for (Iterator iterator = players.iterator(); iterator.hasNext();) {
			PlayerTourInfo pti = (PlayerTourInfo) iterator.next();
			if (!pti.isActive())
				iterator.remove();
		}
		if (players.size() <= 1) // not enough players
			throw new IllegalStateException("Not enought players");
		sortForScheduling(players);
		// add dummy
		addEvenDummy(players);
		scheduleRound(r, players);
		dummyLooses(r);
	}

	protected void addEvenDummy(ArrayList<PlayerTourInfo> players) {
		if (players.size() % 2 == 1) {
			addDummy(players);
		}
	}

	protected PlayerTourInfo addDummy(ArrayList<PlayerTourInfo> players) {
		PlayerTourInfo playerTourInfo = new PlayerTourInfo(Player.DUMMY);
		players.add(playerTourInfo);
		return playerTourInfo;
	}

	/**
	 * @param r
	 * 
	 */
	protected void dummyLooses(Round r) {
		if (r.getNumber() == 0)
			return;
		List<TableInfo> tables = r.getTables();
		for (TableInfo tableInfo : tables) {
			PlayerRoundInfo[] playerRoundInfo = tableInfo.getPlayerRoundInfo();
			if (playerRoundInfo.length != 2)
				continue;
			boolean hasDummy = false;
			for (PlayerRoundInfo pi : playerRoundInfo) {
				if (pi.getPlayer() == Player.DUMMY) {
					hasDummy = true;
					pi.setWinGames(0, 1, 0);
					break;
				}
			}
			if (hasDummy)
				for (PlayerRoundInfo pi : playerRoundInfo) {
					if (pi.getPlayer() != Player.DUMMY) {
						pi.setWinGames(1, 0, 0);
						break;
					}
				}
		}
	}

	protected abstract void scheduleRound(Round r, ArrayList<PlayerTourInfo> players);

	protected void checkType(Round r) {
		if (r.getType() != r.getTournament().getType()) {
			throw new IllegalStateException("Bad scheduler");
		}
	}

	protected void sortForScheduling(ArrayList<PlayerTourInfo> players) {
	}
}
