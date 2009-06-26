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
package com.reflexit.mtgtournament.core.model;

import java.util.ArrayList;
import java.util.List;

public class Cube {
	private PlayerList playerList = new PlayerList();
	private List<Tournament> tournaments = new ArrayList<Tournament>();

	public PlayerList getPlayerList() {
		return playerList;
	}

	public boolean addTournament(Tournament ts) {
		if (tournaments.contains(ts))
			return false;
		tournaments.add(ts);
		ts.setCube(this);
		return true;
	}

	/**
	 * @return
	 */
	public List<Tournament> getTournamens() {
		return tournaments;
	}

	/**
	 * @param t
	 */
	public void removeTournament(Tournament t) {
		tournaments.remove(t);
	}
}
