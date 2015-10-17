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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo.PlayerGameResult;

public class PlayerTourInfo {
	transient private Tournament tournament;
	private Player player;
	private int roundsWon = 0;
	private int roundsDrawn = 0;
	private int roundsLost = 0;
	private int points = 0;
	private int matches = 0;
	private boolean active = true;
	private int place;
	private int gamesWon;
	private int gamesLost;
	private int gamesDrawn;
	private float omw;
	private float ogw;
	private float pgw;
	private Map<Integer, Boolean> byes;
	// unused. Cannot delete because of xstream
	private Set<Player> opponents;

	public PlayerTourInfo(Player player) {
		setPlayer(player);
	}

	@Override
	public String toString() {
		return player + " " + points;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PlayerTourInfo))
			return false;
		PlayerTourInfo other = (PlayerTourInfo) obj;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getLost() {
		return roundsLost;
	}

	public void setLoose(int loose) {
		this.roundsLost = loose;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getRoundsPlayed() {
		return matches;
	}

	public void setMatches(int matches) {
		this.matches = matches;
	}

	/**
	 * @param tournament
	 *            the tournament to set
	 */
	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}

	/**
	 * @return the tournament
	 */
	public Tournament getTournament() {
		return tournament;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return the draw
	 */
	public int getDraw() {
		return roundsDrawn;
	}

	/**
	 * @return the win matches
	 */
	public int getWin() {
		return roundsWon;
	}

	public void calclulateOMW(int n) {
		HashSet<Player> opponents = new HashSet<Player>();
		List<Round> rounds = tournament.getRounds();
		for (Round round : rounds) {
			if (round.getNumber() > n)
				break;
			if (round.getNumber() == 0) continue; // XXX draft round
			for (TableInfo tableInfo : round.getTables()) {
				for (PlayerRoundInfo pi : tableInfo.getPlayerRoundInfo()) {
					if (pi.getPlayer() == getPlayer()) {
						addOpponents(tableInfo, opponents);
						break;
					}
				}
			}
		}
		int w = 0;
		int gw = 0;
		int m = 0;
		int gm = 0;
		for (Iterator<Player> iterator = opponents.iterator(); iterator.hasNext();) {
			Player next = iterator.next();
			PlayerTourInfo oppInfo = tournament.findPlayerTourInfo(next);
			if (oppInfo != null) {
				w += oppInfo.getWin();
				m += oppInfo.getRoundsPlayed();
				gw += oppInfo.getGamesWon();
				gm += oppInfo.getGamesPlayed();
			}
		}
		if (m == 0)
			omw = 0;
		else
			omw = 100 * w / m;
		if (gm == 0)
			ogw = 0;
		else
			ogw = 100 * gw / gm;
		PlayerTourInfo playerInfo = this;
		gw = playerInfo.getGamesWon();
		gm = playerInfo.getGamesPlayed();
		if (gm == 0)
			pgw = 0;
		else
			pgw = 100 * gw / gm;
	}

	/**
	 * @param place
	 *            the place to set
	 */
	public void setPlace(int place) {
		this.place = place;
	}

	/**
	 * @return the place
	 */
	public int getPlace() {
		return place;
	}

	/**
	 * @param pi
	 */
	public void addMatchResult(PlayerRoundInfo roundInfo) {
		PlayerGameResult result = roundInfo.getResult();
		matches++;
		switch (result) {
			case WIN:
				roundsWon++;
				break;
			case LOOSE:
				roundsLost++;
				break;
			case DRAW:
				roundsDrawn++;
				break;
			case _NONE:
				return;
			default:
				break;
		}
		gamesWon += roundInfo.getWin();
		gamesLost += roundInfo.getLost();
		gamesDrawn += roundInfo.getDraw();
		updatePoints();
	}

	/**
	 *
	 */
	private void updatePoints() {
		points = roundsWon * getTournament().getPointsPerWin()
				+ roundsDrawn * getTournament().getPointsPerDraw()
				+ roundsLost * getTournament().getPointsPerLoss();
	}

	/**
	 *
	 */
	public void resetPoints() {
		roundsWon = 0;
		roundsLost = 0;
		roundsDrawn = 0;
		matches = 0;
		points = 0;
		gamesDrawn = 0;
		gamesLost = 0;
		gamesWon = 0;
	}

	/**
	 * @param np
	 */
	public void setPlayer(Player np) {
		if (np == null)
			throw new NullPointerException();
		this.player = np;
	}

	private void addOpponents(TableInfo tableInfo, Collection<Player> opponents) {
		PlayerRoundInfo[] playerRoundInfo = tableInfo.getPlayerRoundInfo();
		for (int i = 0; i < playerRoundInfo.length; i++) {
			PlayerRoundInfo ri = playerRoundInfo[i];
			if (ri.getPlayer() != player) {
				opponents.add(ri.getPlayer());
			}
		}
	}

	public int getGamesPlayed() {
		return gamesWon + gamesLost + gamesDrawn;
	}

	public int getGamesWon() {
		return gamesWon;
	}

	public int getGamesLost() {
		return gamesLost;
	}

	public int getGamesDrawn() {
		return gamesDrawn;
	}

	public float getOMW() {
		return omw;
	}

	public float getOGW() {
		return ogw;
	}

	public float getPGW() {
		return pgw;
	}

	public Map<Integer, Boolean> getByes() {
		if (byes == null)
			byes = new HashMap<Integer, Boolean>();
		return byes;
	}

	public boolean getBye(int round) {
		Boolean b = getByes().get(round);
		if (b == null)
			return false;
		return b;
	}

	public static Player[] toPlayers(PlayerTourInfo... ptis) {
		Player[] res = new Player[ptis.length];
		for (int i = 0; i < ptis.length; i++) {
			res[i] = ptis[i].getPlayer();
		}
		return res;
	}
}
