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
	private int w = -1;
	private PlayerGameResult r = null;
	public enum PlayerGameResult {
		WIN,
		LOOSE,
		DRAW
	}

	public PlayerGameResult getResult() {
		return r;
	}

	public void setWinGames(int w) {
		this.setWin(w);
	}

	public void setResult(PlayerGameResult r) {
		this.r = r;
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
	 * @param w the w to set
	 */
	void setWin(int w) {
		this.w = w;
	}

	/**
	 * @return the w
	 */
	int getWin() {
		return w;
	}
}
