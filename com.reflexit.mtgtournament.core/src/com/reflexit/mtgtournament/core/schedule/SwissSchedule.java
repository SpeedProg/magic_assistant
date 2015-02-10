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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import com.reflexit.mtgtournament.core.edit.CmdAddTable;
import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
 * @author Alena
 *
 */
public class SwissSchedule extends AbstractScheduler {
	class StateInfo {
		List<PlayerTourInfo> players;
		int cand;
		public CmdAddTable command;

		public StateInfo(List<PlayerTourInfo> players, int cand, CmdAddTable command) {
			super();
			this.players = new ArrayList<PlayerTourInfo>(players);
			this.cand = cand;
			this.command = command;
		}
	}

	@Override
	protected void scheduleRound(Round r, List<PlayerTourInfo> players) {
		int opp = r.getOpponentsPerGame();
		if (opp != 2)
			throw new IllegalArgumentException("Cannot schedule swiss for " + opp
					+ " opponents, this only works for 2 opponents");
		int roundNumber = r.getNumber();
		addDummies(players, opp);
		// prepare undo stack
		Tournament t = r.getTournament();
		Stack<StateInfo> stack = new Stack<StateInfo>();
		ArrayList<PlayerTourInfo> unmatched = new ArrayList<PlayerTourInfo>();
		int cand = 1;
		while (players.size() > 1) {
			PlayerTourInfo pti1 = players.get(0);
			Player p1 = pti1.getPlayer();
			boolean paired = false;
			for (; paired == false && cand < players.size(); cand++) {
				PlayerTourInfo pti2 = players.get(cand);
				Player p2 = pti2.getPlayer();
				if (!t.hasPlayed(p1, p2, roundNumber - 1)) {
					// command to schedule a table
					CmdAddTable com = addTable(r, pti1, pti2);
					// save backtracking info
					StateInfo info = new StateInfo(players, cand, com);
					stack.add(info);
					paired = true;
					players.remove(pti1);
					players.remove(pti2);
					cand = 1; // next pick starts with 1
					// System.err.println("Scheduled: " + p1 + " vs " + p2);
					break;
				} else {
					// System.err.println("Played: " + p1 + " vs " + p2);
				}
			}
			if (paired == false) {
				if (!stack.isEmpty()) {
					// System.err.println("Backtracking");
					// restore backtracking values
					StateInfo info = stack.pop();
					players = info.players;
					info.command.undo();
					cand = info.cand + 1; // backtracking - next pick +1
				} else {
					// cannot backtrack anymore - move on
					players.remove(pti1);
					unmatched.add(pti1);
					cand = 1;
				}
			}
		}
		if (players.size() > 0) {
			unmatched.addAll(players);
		}
		// when number of players are close to number of tours swiss cannot
		// guarantee no conflicts
		// going to resolve conflicts randomly
		new RandomSchedule().scheduleRound(r, unmatched);
	}

	@Override
	public TournamentType getType() {
		return TournamentType.SWISS;
	}

	@Override
	protected void sortForScheduling(List<PlayerTourInfo> players) {
		Collections.shuffle(players); // ramdomize for player with same rating
		Collections.sort(players, new Comparator<PlayerTourInfo>() {
			public int compare(PlayerTourInfo a, PlayerTourInfo b) {
				return Tournament.comparePlayers(a, b);
			}
		});
	}
}
