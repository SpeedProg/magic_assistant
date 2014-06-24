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

import com.reflexit.mtgtournament.core.model.TableInfo;

/**
 * Command to add Round Table Scheduling into the round
 */
public class CmdDeleteTable implements ITCommand {
	private TableInfo tableInfo;

	public CmdDeleteTable(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	public boolean execute() {
		return tableInfo.getRound().getTables().remove(tableInfo);
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public boolean undo() {
		// int tab = tableInfo.getRound().getTables().size()+1;
		tableInfo.getRound().addTable(tableInfo);
		return true;
	}
}
