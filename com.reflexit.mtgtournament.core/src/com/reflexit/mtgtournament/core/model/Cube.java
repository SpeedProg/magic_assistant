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
		tournaments.add(ts);
		ts.setCube(this);
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public Collection<Player> getPlayers() {
		return players;
	}
}
