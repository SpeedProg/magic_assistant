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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
 * Schedule players randomly. It does not take into account history. See
 * pseudo-random for history consideration
 *
 * @author Alena
 *
 */
public class RandomSchedule extends AbstractScheduler {
	@Override
	protected void scheduleRound(Round r, List<PlayerTourInfo> players) {
		int opp = r.getOpponentsPerGame();
		addDummies(players, opp);
		if (players.size() % opp != 0) throw new IllegalArgumentException();
		for (Iterator<PlayerTourInfo> iterator = players.iterator(); iterator.hasNext();) {
			Player[] res = new Player[opp];
			for (int i = 0; i < opp; i++) {
				res[i] = iterator.next().getPlayer();
			}
			addTable(r, res);
		}
	}

	@Override
	protected void sortForScheduling(List<PlayerTourInfo> players) {
		Collections.shuffle(players);
	}

	@Override
	public TournamentType getType() {
		return TournamentType.RANDOM;
	}
}
