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

import java.util.HashSet;
import java.util.Iterator;
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
	private HashSet<Player> opponents = new HashSet<Player>();
	private int gamesWon;
	private int gamesLost;
	private int gamesDrawn;
	private float omw;
	private float ogw;
	private float pgw;

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

	public void calclulateOMW() {
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
				gm += oppInfo.getGamesWon() + oppInfo.getGamesLost() + oppInfo.getGamesDrawn();
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
		gm = playerInfo.getGamesWon() + playerInfo.getGamesLost() + playerInfo.getGamesDrawn();
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
		if (result == null)
			return;
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
			default:
				break;
		}
		gamesWon += roundInfo.getWin();
		gamesLost += roundInfo.getLost();
		gamesDrawn += roundInfo.getDraw();
		updatePoints();
		addOpponents(roundInfo.getTableInfo());
	}

	/**
	 * 
	 */
	private void updatePoints() {
		points = roundsWon * 2 + roundsDrawn * 1 + roundsLost * 0;
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

	/**
	 * Return list of opponents this player faced in this tournament
	 * 
	 * @return
	 */
	public Set<Player> getOpponents() {
		return opponents;
	}

	private void addOpponents(TableInfo tableInfo) {
		PlayerRoundInfo[] playerRoundInfo = tableInfo.getPlayerRoundInfo();
		for (int i = 0; i < playerRoundInfo.length; i++) {
			PlayerRoundInfo ri = playerRoundInfo[i];
			if (ri.getPlayer() != player) {
				opponents.add(ri.getPlayer());
			}
		}
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
}