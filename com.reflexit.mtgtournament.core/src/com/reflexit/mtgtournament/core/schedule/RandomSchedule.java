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

import com.reflexit.mtgtournament.core.edit.ComAddTable;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
 * @author Alena
 *
 */
public class RandomSchedule extends AbstractScheduler implements IScheduler {
	@Override
	public void schedule(Tournament t) {
		super.schedule(t);
		for (int i = 0; i <= t.getNumberOfRounds(); i++) {
			Round r = t.getRound(i);
			schedule(r);
		}
	}

	@Override
	protected void scheduleRound(Round r, ArrayList<PlayerTourInfo> players) {
		int table = 1;
		// this method has even number of players always
		while (players.size() > 1) {
			PlayerTourInfo pti1 = players.get(0);
			PlayerTourInfo pti2 = players.get(1);
			ComAddTable com = new ComAddTable(r, table, pti1.getPlayer(), pti2.getPlayer());
			com.execute();
			table++;
			players.remove(pti1);
			players.remove(pti2);
		}
	}

	@Override
	protected void sortForScheduling(ArrayList<PlayerTourInfo> players) {
		Collections.shuffle(players);
	}

	@Override
	protected void checkType(Round r) {
		if (r.getType() != TournamentType.RANDOM) {
			throw new IllegalStateException("Bad scheduler");
		}
	}
}
