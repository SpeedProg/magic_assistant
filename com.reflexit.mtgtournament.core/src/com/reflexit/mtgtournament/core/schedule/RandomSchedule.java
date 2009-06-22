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
public class RandomSchedule {
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
			r.setType(t.getType());
			schedule(r);
		}
	}

	public void schedule(Round r) {
		if (r.getType() != TournamentType.RANDOM) {
			throw new IllegalStateException("Bad scheduler");
		}
		if (r.getState() != RoundState.NOT_SCHEDULED) {
			throw new IllegalStateException("Round is not ready or already scheduled");
		}
		Tournament t = r.getTournament();
		ArrayList<PlayerTourInfo> players = new ArrayList<PlayerTourInfo>(t.getPlayersInfo());
		Collections.shuffle(players);
		int table = 0;
		while (players.size() > 1) {
			PlayerTourInfo pti1 = players.get(0);
			if (!pti1.isActive())
				continue;
			PlayerTourInfo pti2 = players.get(1);
			if (!pti2.isActive())
				continue;
			PlayerRoundInfo pr1 = r.addPlayer(pti1);
			PlayerRoundInfo pr2 = r.addPlayer(pti2);
			TableInfo tableInfo = new TableInfo(table, r, pr1, pr2);
			table++;
			r.addTable(tableInfo);
			players.remove(pti1);
			players.remove(pti2);
		}
		if (players.size() > 0) {
			PlayerRoundInfo dummy = r.addDummy();
			PlayerTourInfo pti3 = players.get(0);
			PlayerRoundInfo pr = r.addPlayer(pti3);
			TableInfo tableInfo = new TableInfo(table, r, pr, dummy);
			table++;
			r.addTable(tableInfo);
		}
	}
}
