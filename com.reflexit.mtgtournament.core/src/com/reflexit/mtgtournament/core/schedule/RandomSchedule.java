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

import com.reflexit.mtgtournament.core.edit.CmdAddTable;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
 * @author Alena
 * 
 */
public class RandomSchedule extends AbstractScheduler implements IScheduler {
	@Override
	protected void addEvenDummy(ArrayList<PlayerTourInfo> players) {
		// no
	}

	@Override
	protected void scheduleRound(Round r, ArrayList<PlayerTourInfo> players) {
		int table = 1;
		while (players.size() > 0) {
			PlayerTourInfo pti1 = players.get(0);
			players.remove(pti1);
			PlayerTourInfo pti2 = null;
			int roundNumber = r.getNumber();
			if (pti1.getBye(roundNumber) || players.size() < 2) {
				pti2 = addDummy(players);
			} else {
				for (PlayerTourInfo info : players) {
					if (!info.getBye(roundNumber)) {
						pti2 = info;
						break;
					}
				}
				if (pti2 == null) {
					pti2 = addDummy(players);
				}
			}
			CmdAddTable com = new CmdAddTable(r, table, pti1.getPlayer(), pti2.getPlayer());
			com.execute();
			players.remove(pti2);
			table++;
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
