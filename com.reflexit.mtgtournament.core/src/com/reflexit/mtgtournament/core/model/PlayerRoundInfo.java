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

public class PlayerRoundInfo {
	transient Round round;
	transient TableInfo tableInfo;
	private Player p;
	private int gamesWon = -1;
	private PlayerGameResult r = null;
	private int gamesLost;
	private int gamesDraw;

	public enum PlayerGameResult {
		WIN,
		LOOSE,
		DRAW
	}

	public PlayerGameResult getResult() {
		return r;
	}

	public void setWinGames(int w, int l, int d) {
		gamesWon = w;
		gamesLost = l;
		gamesDraw = d;
		if (w > l) {
			this.r = PlayerGameResult.WIN;
		} else if (w < l) {
			this.r = PlayerGameResult.LOOSE;
		} else {
			this.r = PlayerGameResult.DRAW;
		}
	}

	public PlayerRoundInfo(Player player, Round round) {
		this.setPlayer(player);
		this.round = round;
	}

	@Override
	public String toString() {
		String s = getWinStr(getResult());
		return getPlayer() + " " + s + "(" + (getWin() == -1 ? "_" : getWin()) + ")";
	}

	public static String getWinStr(PlayerGameResult result) {
		String s = "_";
		if (result == PlayerGameResult.LOOSE)
			s = "L";
		else if (result == PlayerGameResult.DRAW)
			s = "D";
		else if (result == PlayerGameResult.WIN)
			s = "W";
		return s;
	}

	public Player getPlayer() {
		return p;
	}

	/**
	 * @param np
	 */
	public void setPlayer(Player np) {
		if (np == null)
			throw new NullPointerException();
		this.p = np;
	}

	/**
	 * @param w
	 *            the w to set
	 */
	void setWin(int w) {
		this.gamesWon = w;
	}

	/**
	 * @return the games win within the match
	 */
	int getWin() {
		return gamesWon;
	}

	int getLost() {
		return gamesLost;
	}

	int getDraw() {
		return gamesDraw;
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public void setTableInfo(TableInfo tableInfo2) {
		tableInfo = tableInfo2;
	}
}
