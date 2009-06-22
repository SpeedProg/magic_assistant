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
import java.util.Arrays;

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
public class SwissSchedule {
	public void schedule(Tournament t) {
		int x = t.getNumberOfRounds();
		if (x == 0) {
			int p = t.getNumberOfPlayers();
			x = p - 2;
			if (x < 1)
				x = 1;
			t.setNumberOfRounds(x);
		}
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

	public void schedule(Round r) {
		if (r.getType() != TournamentType.SWISS) {
			throw new IllegalStateException("Bad scheduler");
		}
		if (r.getState() != RoundState.NOT_SCHEDULED) {
			throw new IllegalStateException("Round is not ready or already scheduled");
		}
		Tournament t = r.getTournament();
		PlayerTourInfo[] pti = t.updatePlace();
		ArrayList<PlayerTourInfo> players = new ArrayList<PlayerTourInfo>(Arrays.asList(pti));
		int table = 0;
		ArrayList<PlayerTourInfo> unmatched = new ArrayList<PlayerTourInfo>();
		while (players.size() > 0) {
			PlayerTourInfo pti1 = players.get(0);
			Player p1 = pti1.getPlayer();
			if (!pti1.isActive())
				continue;
			int cand = 1;
			boolean paired = false;
			for (cand = 1; paired == false && cand < players.size(); cand++) {
				PlayerTourInfo pti2 = players.get(cand);
				if (!pti2.isActive())
					continue;
				Player p2 = pti2.getPlayer();
				if (!t.hasPlayed(p1, p2, r.getNumber() - 1)) {
					PlayerRoundInfo pr1 = r.addPlayer(pti1);
					PlayerRoundInfo pr2 = r.addPlayer(pti2);
					TableInfo tableInfo = new TableInfo(table, r, pr1, pr2);
					table++;
					r.addTable(tableInfo);
					paired = true;
					players.remove(pti1);
					players.remove(pti2);
				} else {
					System.err.println("Played: " + p1 + " vs " + p2);
				}
			}
			if (paired == false) {
				players.remove(pti1);
				unmatched.add(pti1);
			}
		}
		if (players.size() > 0) {
			unmatched.addAll(players);
		}
		PlayerRoundInfo dummy = r.addDummy();
		for (Object element : unmatched) {
			PlayerTourInfo pti3 = (PlayerTourInfo) element;
			PlayerRoundInfo pr = r.addPlayer(pti3);
			TableInfo tableInfo = new TableInfo(table, r, pr, dummy);
			table++;
			r.addTable(tableInfo);
		}
	}
}
