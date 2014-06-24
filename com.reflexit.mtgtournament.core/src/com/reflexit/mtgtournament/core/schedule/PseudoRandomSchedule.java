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

import java.util.Collections;
import java.util.List;

import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
 * Similar to Swiss to players are not actually matched based on skill, but
 * randomly. Each tour it will try to to not schedule same pair twice but there
 * is no guarantee if number of players close to number of rounds.
 * 
 */
public class PseudoRandomSchedule extends SwissSchedule {
	@Override
	public TournamentType getType() {
		return TournamentType.PSEUDO_RANDOM;
	}

	@Override
	protected void sortForScheduling(List<PlayerTourInfo> players) {
		Collections.shuffle(players);
	}
}
