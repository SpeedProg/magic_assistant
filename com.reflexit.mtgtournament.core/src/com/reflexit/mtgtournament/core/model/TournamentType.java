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

import com.reflexit.mtgtournament.core.schedule.CompositeScheduler;
import com.reflexit.mtgtournament.core.schedule.EliminationScheduler;
import com.reflexit.mtgtournament.core.schedule.IScheduler;
import com.reflexit.mtgtournament.core.schedule.PseudoRandomSchedule;
import com.reflexit.mtgtournament.core.schedule.RandomSchedule;
import com.reflexit.mtgtournament.core.schedule.RoundRobinSchedule;
import com.reflexit.mtgtournament.core.schedule.SwissSchedule;

public enum TournamentType {
	ROUND_ROBIN(new RoundRobinSchedule()),
	SWISS(new SwissSchedule()),
	ELIMINATION(new EliminationScheduler()),
	RANDOM(new RandomSchedule()),
	PSEUDO_RANDOM(new PseudoRandomSchedule()),
	COMPOSITE(new CompositeScheduler());
	private IScheduler scheduler;

	TournamentType(IScheduler scheduler) {
		this.scheduler = scheduler;
	};

	public IScheduler getScheduler() {
		return scheduler;
	}

	/**
	 * @return
	 */
	public static String[] stringValues() {
		TournamentType[] values = values();
		String res[] = new String[values.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = values[i].name();
		}
		return res;
	}

	/**
	 * @param intValue
	 * @return
	 */
	public static TournamentType valueOf(int intValue) {
		TournamentType[] values = values();
		for (TournamentType tt : values) {
			if (tt.ordinal() == intValue)
				return tt;
		}
		return null;
	}
}