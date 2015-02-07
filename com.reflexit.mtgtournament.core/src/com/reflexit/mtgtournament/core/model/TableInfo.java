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

import java.util.Arrays;

public class TableInfo {
	private transient Round round;
	private transient int table;
	private PlayerRoundInfo[] pi;

	public TableInfo(PlayerRoundInfo... opponents) {
		super();
		if (opponents.length < 2)
			throw new IllegalArgumentException("Minimum 2 oppoents are expecteds");
		this.table = -1;
		this.pi = opponents;
		this.round = opponents[0].round;
		for (PlayerRoundInfo pri : opponents) {
			pri.setTableInfo(this);
		}
	}

	public TableInfo(Round round, Player... ps) {
		this.round = round;
		PlayerRoundInfo pi[] = new PlayerRoundInfo[ps.length];
		for (int i = 0; i < ps.length; i++) {
			pi[i] = round.createOpponentInfo(ps[i]);
			pi[i].setTableInfo(this);
		}
	}

	public int getOpponentsPerGame() {
		return pi.length;
	}

	@Override
	public String toString() {
		if (pi.length == 2)
			return table + ": " + getOpponent(0) + " vs " + getOpponent(1);
		else
			return table + ": " + Arrays.asList(pi);
	}

	public int getTableNumber() {
		return table;
	}

	public Round getRound() {
		return round;
	}

	public PlayerRoundInfo getOpponent(int i) {
		if (i >= pi.length || i < 0) return null;
		return pi[i];
	}

	/**
	 * @param round
	 *            the round to set
	 */
	public void setRound(Round round) {
		this.round = round;
	}

	public void updateLinks() {
		for (PlayerRoundInfo pp : pi) {
			Player np = round.getTournament().findPlayer(pp.getPlayer());
			pp.setPlayer(np);
		}
	}

	public PlayerRoundInfo[] getPlayerRoundInfo() {
		return pi;
	}

	public void setNumber(int num) {
		this.table = num;
	}
}
