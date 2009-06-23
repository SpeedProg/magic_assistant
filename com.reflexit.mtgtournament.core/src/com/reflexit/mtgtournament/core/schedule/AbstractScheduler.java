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

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.RoundState;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
 * @author Alena
 *
 */
public abstract class AbstractScheduler implements IScheduler {
	public void schedule(Tournament t) {
		updateRoundNumber(t);
		for (int i = 0; i <= t.getNumberOfRounds(); i++) {
			Round r = new Round(i);
			t.addRound(r);
			if (i == 0) {
				r.setType(TournamentType.RANDOM);
				if (!t.isDraftRound()) {
					r.schedule();
					r.close();
				}
			} else
				r.setType(t.getType());
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
		//System.err.println("Round ======" + r.getNumber());
		// get active players and sort them by place
		ArrayList<PlayerTourInfo> players = new ArrayList<PlayerTourInfo>(r.getTournament().getPlayersInfo());
		for (Iterator iterator = players.iterator(); iterator.hasNext();) {
			PlayerTourInfo pti = (PlayerTourInfo) iterator.next();
			if (!pti.isActive())
				iterator.remove();
		}
		if (players.size() <= 1) // not enough players
			throw new IllegalStateException("Not enough players");
		sortForScheduling(players);
		// add dummy
		if (players.size() % 2 == 1) {
			players.add(new PlayerTourInfo(Player.DUMMY));
		}
		scheduleRound(r, players);
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
