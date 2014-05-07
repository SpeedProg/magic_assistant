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
public class EliminationScheduler extends AbstractScheduler {
	@Override
	protected void checkType(Round r) {
		if (r.getType() != TournamentType.ELIMINATION) {
			throw new IllegalStateException("Bad scheduler: " + r.getType());
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

	@Override
	protected void updateRoundNumber(Tournament t) {
		int x = t.getNumberOfRounds();
		if (x == 0) {
			int p = t.getNumberOfPlayers();
			while (p > 1) {
				if (p % 2 == 0) {
					p = p / 2;
					x++;
				} else {
					p = p + 1;
					//x++;
				}
			}
			t.setNumberOfRounds(x);
		}
	}

	@Override
	protected void scheduleRound(Round r, ArrayList<PlayerTourInfo> players) {
		int pow = r.getTournament().getNumberOfRounds() - r.getNumber() + 1;
		int val = 1;
		for (int i = 1; i < pow; i++) {
			val = val * 2;
		}
		val = val * 2;
		// number of players needed for round.
		// if more delete extra
		while (players.size() > val) {
			players.remove(players.size() - 1);
		}
		// if less than required add dummies
		while (players.size() < val) {
			players.add(new PlayerTourInfo(Player.DUMMY));
		}
		int table = 1;
		while (players.size() > 0) {
			PlayerTourInfo pti1 = players.get(0);
			PlayerTourInfo pti2 = players.get(players.size() - 1);
			CmdAddTable com = new CmdAddTable(r, table, pti1.getPlayer(), pti2.getPlayer());
			com.execute();
			table++;
			players.remove(pti1);
			players.remove(pti2);
		}
	}
}
