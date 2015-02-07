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
public class CmdAddTable implements ITCommand {
	private TableInfo tableInfo;

	public CmdAddTable(Round round, Player... ps) {
		super();
		if (ps.length < 2) throw new IllegalArgumentException("Minium 2 opponents are required");
		PlayerRoundInfo pi[] = new PlayerRoundInfo[ps.length];
		for (int i = 0; i < ps.length; i++) {
			pi[i] = round.createOpponentInfo(ps[i]);
		}
		tableInfo = new TableInfo(pi);
	}

	public CmdAddTable(PlayerRoundInfo... ps) {
		super();
		if (ps.length < 2) throw new IllegalArgumentException("Minium 2 opponents are required");
		tableInfo = new TableInfo(ps);
	}

	public boolean execute() {
		getRound().addTable(tableInfo);
		return true;
	}

	public boolean undo() {
		return getRound().getTables().remove(tableInfo);
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public Round getRound() {
		return tableInfo.getRound();
	}
}
