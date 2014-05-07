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
package com.reflexit.mtgtournament.core.schedule;

import java.util.ArrayList;
import java.util.Collections;

import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
 * Similar to Swiss to players are not actually matched based on skill, but randomly.
 * Each tour it will try to to not schedule same pair twice but there is no guarantee
 * if number of players close to number of rounds.
 *
 */
public class PseudoRandomSchedule extends SwissSchedule {
	@Override
	protected void checkType(Round r) {
		if (r.getType() != TournamentType.PSEUDO_RANDOM) {
			throw new IllegalStateException("Bad scheduler");
		}
	}

	@Override
	protected void sortForScheduling(ArrayList<PlayerTourInfo> players) {
		Collections.shuffle(players);
	}
}
