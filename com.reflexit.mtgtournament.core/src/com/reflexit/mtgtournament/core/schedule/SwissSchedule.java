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
import java.util.Iterator;
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
		ArrayList<PlayerTourInfo> players;
		int table;
		int cand;
		public CmdAddTable command;
	}

	@Override
	protected void scheduleRound(Round r, ArrayList<PlayerTourInfo> players) {
		// prepare undo stack
		Tournament t = r.getTournament();
		Stack<StateInfo> stack = new Stack<StateInfo>();
		int table = 1;
		ArrayList<PlayerTourInfo> unmatched = new ArrayList<PlayerTourInfo>();
		int cand = 1;
		while (players.size() > 1) {
			PlayerTourInfo pti1 = players.get(0);
			Player p1 = pti1.getPlayer();
			boolean paired = false;
			for (; paired == false && cand < players.size(); cand++) {
				PlayerTourInfo pti2 = players.get(cand);
				Player p2 = pti2.getPlayer();
				if (!t.hasPlayed(p1, p2, r.getNumber() - 1)) {
					// command to schedule a table
					CmdAddTable com = new CmdAddTable(r, table, p1, p2);
					// save backtracking info
					StateInfo info = new StateInfo();
					info.players = (ArrayList<PlayerTourInfo>) players.clone();
					info.table = table;
					info.cand = cand;
					info.command = com;
					stack.add(info);
					// execute command and advance
					com.execute();
					table++;
					paired = true;
					players.remove(pti1);
					players.remove(pti2);
					cand = 1; // next pick starts with 1
					//	System.err.println("Scheduled: " + p1 + " vs " + p2);
					break;
				} else {
					//	System.err.println("Played: " + p1 + " vs " + p2);
				}
			}
			if (paired == false) {
				if (!stack.isEmpty()) {
					//	System.err.println("Backtracking");
					// restore backtracking values
					StateInfo info = stack.pop();
					players = info.players;
					table = info.table;
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
		// when number of players are close to number of tours swiss cannot guarantee no conflicts 
		// going to resolve conflicts randomly
		for (Iterator iterator = unmatched.iterator(); iterator.hasNext();) {
			PlayerTourInfo pti1 = (PlayerTourInfo) iterator.next();
			PlayerTourInfo pti2 = (PlayerTourInfo) iterator.next();
			CmdAddTable com = new CmdAddTable(r, table, pti1.getPlayer(), pti2.getPlayer());
			com.execute();
			//System.err.println("Scheduled (conf): " + pti1.getPlayer() + " vs " + pti2.getPlayer());
		}
	}

	@Override
	protected void checkType(Round r) {
		if (r.getType() != TournamentType.SWISS) {
			throw new IllegalStateException("Bad scheduler");
		}
	}

	@Override
	protected void sortForScheduling(ArrayList<PlayerTourInfo> players) {
		Collections.sort(players, new Comparator<PlayerTourInfo>() {
			public int compare(PlayerTourInfo a, PlayerTourInfo b) {
				return Tournament.comparePlayers(a, b);
			}
		});
	}
}
