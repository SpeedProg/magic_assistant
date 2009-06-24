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
package com.reflexit.mtgtournament.core.edit;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TableInfo;

/**
 * Command to add Round Table Scheduling into the round
 */
public class ComAddTable implements ITCommand {
	private TableInfo tableInfo;
	private Round round;

	public ComAddTable(Round round, int table, Player p1, Player p2) {
		super();
		PlayerRoundInfo pr1 = round.makePlayer(p1);
		PlayerRoundInfo pr2 = round.makePlayer(p2);
		this.round = round;
		tableInfo = new TableInfo(table, pr1, pr2);
	}

	public boolean execute() {
		round.addTable(tableInfo);
		return true;
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public boolean undo() {
		return tableInfo.getRound().getTables().remove(tableInfo);
	}
}
