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
	private PlayerRoundInfo p1;
	private PlayerRoundInfo p2;

	public TableInfo(int table, PlayerRoundInfo p1, PlayerRoundInfo p2) {
		super();
		this.table = table;
		this.setP1(p1);
		this.setP2(p2);
	}

	@Override
	public String toString() {
		return table + ": " + getP1() + " vs " + getP2();
	}

	public int getTableNumber() {
		return table;
	}

	public Round getRound() {
		return round;
	}

	public PlayerRoundInfo getP1() {
		return p1;
	}

	public PlayerRoundInfo getP2() {
		return p2;
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
    public void setP1(PlayerRoundInfo p1) {
	    this.p1 = p1;
    }

	/**
     * @param p2 the p2 to set
     */
    public void setP2(PlayerRoundInfo p2) {
	    this.p2 = p2;
    }
}
