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
	transient int table;
	transient Round round;
	PlayerRoundInfo p1;
	PlayerRoundInfo p2;

	public TableInfo(int table, Round round, PlayerRoundInfo p1, PlayerRoundInfo p2) {
		super();
		this.table = table;
		this.round = round;
		this.p1 = p1;
		this.p2 = p2;
	}

	@Override
	public String toString() {
		return table + ": " + p1 + " vs " + p2;
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
}
