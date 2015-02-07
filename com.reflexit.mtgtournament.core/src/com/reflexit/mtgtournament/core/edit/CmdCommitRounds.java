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
package com.reflexit.mtgtournament.core.edit;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo.PlayerGameResult;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;

/**
 * Command to add Round Table Scheduling into the round
 */
public class CmdCommitRounds implements ITCommand {
	private Tournament t;
	private int n;

	public CmdCommitRounds(Tournament t, int roundsToCommit) {
		super();
		this.t = t;
		this.n = roundsToCommit;
	}

	public boolean execute() {
		updateStandings();
		return true;
	}

	public boolean undo() {
		for (PlayerTourInfo ti : t.getPlayersInfo()) {
			ti.resetPoints();
		}
		return true;
	}

	public void updateStandings() {
		for (PlayerTourInfo ti : t.getPlayersInfo()) {
			ti.resetPoints();
		}
		for (Round r : t.getRounds()) {
			if (r.getNumber() > n)
				break;
			for (Object element : r.getTables()) {
				TableInfo t = (TableInfo) element;
				for (int i = 0; i < t.getPlayerRoundInfo().length; i++) {
					PlayerRoundInfo pi = t.getPlayerRoundInfo()[i];
					pi.setTableInfo(t);
					updateInfo(pi);
				}
			}
		}
		updatePlace();
	}

	private void updateInfo(PlayerRoundInfo pi) {
		if (pi.getPlayer().isDummy())
			return;
		PlayerTourInfo pt = t.findPlayerTourInfo(pi.getPlayer());
		if (pi.getResult() != PlayerGameResult._NONE)
			pt.addMatchResult(pi);
	}

	public PlayerTourInfo[] updatePlace() {
		List<PlayerTourInfo> players = t.getPlayersInfo();
		for (Iterator iterator = players.iterator(); iterator.hasNext();) {
			PlayerTourInfo pti = (PlayerTourInfo) iterator.next();
			pti.calclulateOMW(n);
		}
		PlayerTourInfo[] pti = players.toArray(new PlayerTourInfo[players.size()]);
		Arrays.sort(pti, new Comparator<PlayerTourInfo>() {
			public int compare(PlayerTourInfo a, PlayerTourInfo b) {
				return Tournament.comparePlayers(a, b);
			}
		});
		int place = 1;
		for (int i = 0; i < pti.length; i++) {
			PlayerTourInfo ti = pti[i];
			if (i > 0) {
				if (Tournament.comparePlayers(ti, pti[i - 1]) != 0) {
					place++;
				}
			}
			ti.setPlace(place);
		}
		return pti;
	}
}
