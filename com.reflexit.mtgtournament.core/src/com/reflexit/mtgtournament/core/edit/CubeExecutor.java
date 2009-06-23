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
import com.reflexit.mtgtournament.core.model.Round;

/**
 * @author Alena
 *
 */
public class CubeExecutor {
	CommandStack stack = new CommandStack();

	public ComAddTable addTableToRound(Round round, int table, Player p1, Player p2) {
		ComAddTable command = new ComAddTable(round, table, p1, p2);
		stack.add(command);
		stack.executeAll();
		return command;
	}
}
