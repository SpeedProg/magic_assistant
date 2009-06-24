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
import java.util.Collection;
import java.util.List;

public class Cube {
	Collection<Player> players = new ArrayList<Player>();
	List<Tournament> tournaments = new ArrayList<Tournament>();

	public void addAllPlayers(Collection<Player> players) {
		this.players.addAll(players);
	}

	public void addTournament(Tournament ts) {
		if (tournaments.contains(ts))
			return;
		tournaments.add(ts);
		ts.setCube(this);
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public Collection<Player> getPlayers() {
		return players;
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
	public void remove(Tournament t) {
		tournaments.remove(t);
	}
}
