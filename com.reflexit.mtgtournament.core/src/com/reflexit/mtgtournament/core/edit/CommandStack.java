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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alena
 *
 */
public class CommandStack {
	private List<ITCommand> commands = new ArrayList<ITCommand>();
	private int undoLimit = 50;
	private int current = 0;

	public boolean executeAll() {
		boolean executed = false;
		while (current < commands.size()) {
			executed |= executeFirst();
		}
		return executed;
	}

	public ITCommand add(ITCommand c) {
		while (commands.size() >= undoLimit) {
			commands.remove(0);
		}
		commands.add(c);
		return c;
	}

	public boolean undo() {
		if (current > 0) {
			current--;
			ITCommand c = commands.get(current);
			return c.undo();
		}
		return false;
	}

	public boolean redo() {
		return executeFirst();
	}

	public boolean executeFirst() {
		if (current < commands.size()) {
			ITCommand c = commands.get(current);
			current++;
			return c.execute();
		}
		return false;
	}
}
