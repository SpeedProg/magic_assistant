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
	private int gamesWon;
	private int gamesDraw;
	private int gamesLost;
	private PlayerGameResult r;

	public enum PlayerGameResult {
		WIN,
		LOOSE,
		DRAW,
		_NONE, ;
		public String letter() {
			return name().substring(0, 1);
		}
	}

	public PlayerGameResult getResult() {
		if (r == null)
			updateResult();
		return r;
	}

	public void setWinGames(int w, int l, int d) {
		gamesWon = w;
		gamesLost = l;
		gamesDraw = d;
		updateResult();
	}

	private void updateResult() {
		if (r == null && gamesWon == -1) { // old way
			r = PlayerGameResult._NONE;
			return;
		}
		if (gamesWon > gamesLost) {
			this.r = PlayerGameResult.WIN;
		} else if (gamesWon < gamesLost) {
			this.r = PlayerGameResult.LOOSE;
		} else {
			this.r = PlayerGameResult.DRAW;
		}
	}

	public PlayerRoundInfo(Player player, Round round) {
		if (round == null) throw new NullPointerException();
		this.round = round;
		setPlayer(player);
		r = PlayerGameResult._NONE;
	}

	@Override
	public String toString() {
		return getPlayer() + " " + getWinStrDetails();
	}

	public String getWinStrDetails() {
		if (getResult() == PlayerGameResult._NONE)
			return "_(_)";
		return r.letter() + "(" + getWin() + ")";
	}

	public Player getPlayer() {
		return p;
	}

	/**
	 * @param newPlayer
	 */
	public final void setPlayer(Player newPlayer) {
		if (newPlayer == null)
			throw new NullPointerException();
		p = newPlayer;
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

	public void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	public static Player[] toPlayers(PlayerRoundInfo... ptis) {
		Player[] res = new Player[ptis.length];
		for (int i = 0; i < ptis.length; i++) {
			res[i] = ptis[i].getPlayer();
		}
		return res;
	}

	public boolean deepEquals(PlayerRoundInfo other) {
		if (this == other) return true;
		if (gamesDraw != other.gamesDraw) return false;
		if (gamesLost != other.gamesLost) return false;
		if (gamesWon != other.gamesWon) return false;
		if (p == null) {
			if (other.p != null) return false;
		} else if (!p.deepEquals(other.p)) return false;
		//if (round != other.round) return false;
		return true;
	}
}
