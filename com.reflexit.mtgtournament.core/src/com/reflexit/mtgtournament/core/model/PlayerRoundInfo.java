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
	Player p;
	int w = -1;
	int r = -1;

	public int getResult() {
		return r;
	}

	public void setResult(int r) {
		this.r = r;
	}

	public PlayerRoundInfo(Player player, Round round) {
		this.p = player;
		this.round = round;
	}

	@Override
	public String toString() {
		String s = getWinStr(r);
		return p + " " + s + "(" + (w == -1 ? "_" : w) + ")";
	}

	public static String getWinStr(int result) {
		String s = "_";
		if (result == 0)
			s = "L";
		else if (result == 1)
			s = "D";
		else if (result == 2)
			s = "W";
		return s;
	}

	public Player getPlayer() {
		return p;
	}
}
