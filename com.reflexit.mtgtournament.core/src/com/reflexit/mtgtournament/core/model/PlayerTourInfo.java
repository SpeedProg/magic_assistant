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

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo.PlayerGameResult;

public class PlayerTourInfo {
	transient private Tournament tournament;
	private Player p;
	private int win = 0;
	private int draw = 0;
	private int loose = 0;
	private int points = 0;
	private int games = 0;
	private boolean active;
	private int place;

	public PlayerTourInfo(Player player) {
		this.p = player;
	}

	@Override
	public String toString() {
		return p + " " + points;
	}

	public Player getPlayer() {
		return p;
	}

	public int getLoose() {
		return loose;
	}

	public void setLoose(int loose) {
		this.loose = loose;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getGames() {
		return games;
	}

	public void setGames(int games) {
		this.games = games;
	}

	/**
	 * @param tournament the tournament to set
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
	 * @param active the active to set
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
	 * @param draw the draw to set
	 */
	public void setDraw(int draw) {
		this.draw = draw;
	}

	/**
	 * @return the draw
	 */
	public int getDraw() {
		return draw;
	}

	/**
	 * @param win the win to set
	 */
	public void setWin(int win) {
		this.win = win;
	}

	/**
	 * @return the win
	 */
	public int getWin() {
		return win;
	}

	/**
	 * @param place the place to set
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
	 * @param result
	 */
	public void addGameResult(PlayerGameResult result) {
		if (result == null)
			return;
		games++;
		switch (result) {
		case WIN:
			win++;
			break;
		case LOOSE:
			loose++;
			break;
		case DRAW:
			draw++;
			break;
		default:
			break;
		}
		updatePoints();
	}

	/**
	 * 
	 */
	private void updatePoints() {
		points = win * 2 + draw * 1 + loose * 0;
	}

	/**
	 * 
	 */
	public void resetPoints() {
		win = 0;
		loose = 0;
		draw = 0;
		games = 0;
		points = 0;
	}
}
