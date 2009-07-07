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

public class TableInfo {
	private transient Round round;
	private int table;
	private PlayerRoundInfo[] pi;

	public TableInfo(int table, PlayerRoundInfo p1, PlayerRoundInfo p2) {
		super();
		this.table = table;
		pi = new PlayerRoundInfo[2];
		this.setPlayerInfo(1, p1);
		this.setPlayerInfo(2, p2);
	}

	@Override
	public String toString() {
		if (pi.length == 2)
			return table + ": " + getPlayerInfo(1) + " vs " + getPlayerInfo(2);
		else
			return table + ": " + pi;
	}

	public int getTableNumber() {
		return table;
	}

	public Round getRound() {
		return round;
	}

	public PlayerRoundInfo getPlayerInfo(int i) {
		return pi[i - 1];
	}

	/**
	 * @param round the round to set
	 */
	public void setRound(Round round) {
		this.round = round;
	}

	/**
	 * @param p1 the p1 to set
	 */
	public void setPlayerInfo(int i, PlayerRoundInfo p1) {
		this.pi[i - 1] = p1;
	}

	/**
	 * 
	 */
	public void updateLinks() {
		for (PlayerRoundInfo pp : pi) {
			Player np = round.getTournament().getCube().getPlayerList().findPlayer(pp.getPlayer());
			if (np != null)
				pp.setPlayer(np);
		}
	}

	public PlayerRoundInfo[] getPlayerRoundInfo() {
		return pi;
	}
}
