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
import java.util.List;

import com.reflexit.mtgtournament.core.edit.CmdAddTable;
import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.TournamentType;

/**
  A round-robin tournament or all-play-all tournament is a type of group tournament 
  "in which each contestant meets every other contestant in turn".
  In a single round-robin schedule, each participant plays every other participant once.
    
If n is the number of competitors, a pure round robin tournament requires (n/2)*(n - 1) games.
If n is even, then in each of (n-1) rounds, n/2 games can be run in parallel,
provided there exist sufficient resources (e.g. courts for a tennis tournament). 
If n is odd, there will be n rounds with (n - 1)/2 games, and one competitor having no game in that round.

The standard algorithm for round-robins is to assign each competitor a number, and pair them off in the first round

Round 1. (1 plays 14, 2 plays 13, ... )
 1  2  3  4  5  6  7  
 14 13 12 11 10 9  8

... then fix one competitor (number one in this example) and rotate the others clockwise …

Round 2. (1 plays 13, 14 plays 12, ... )
 1  14 2  3  4  5  6
 13 12 11 10 9  8  7

Round 3. (1 plays 12, 13 plays 11, ... )
 1  13 14 2  3  4  5
 12 11 10 9  8  7  6

... until you end up almost back at the initial position

Round 13. (1 plays 2, 3 plays 14, ... )
 1  3  4  5  6  7  8
 2 14  13 12 11 10 9

If there are an odd number of competitors, a dummy competitor can be added, 
whose scheduled opponent in a given round does not play and has a bye. 
The upper and lower rows can indicate home/away in sports, white/black in chess, etc 
(this must alternate between rounds since competitor 1 is always on the first row). 
If, say, competitors 3 and 8 were unable to fulfill their fixture in the third round, 
it would need to be rescheduled outside the other rounds, since both competitors would already be facing 
other opponents in those rounds. 


 * @author Alena
 *
 */
public class RoundRobinSchedule implements IScheduler {
	public void schedule(Tournament t) {
		int pn = t.getNumberOfPlayers();
		int tables = pn / 2 + pn % 2;
		int pna = tables * 2; // with odd number add dummy
		int positions[] = new int[pna];
		for (int i = 0; i < tables; i++) {
			positions[i] = i;
			positions[i + tables] = pna - 1 - i;
		}
		if (t.getNumberOfRounds() == 0) {
			t.setNumberOfRounds(pna - 1); // plus draft
		}
		List<Player> array = init(t.getPlayersInfo());
		Collections.shuffle(array);
		for (int i = 0; i <= t.getNumberOfRounds(); i++) {
			Round r = new Round(i);
			t.addRound(r);
			r.setType(t.getType());
			if (i == 0) {
				r.setType(TournamentType.RANDOM);
				r.schedule();
				continue;
			}
			for (int j = 0; j < tables; j++) {
				int n = (j + i) % tables;
				int pos1 = positions[n];
				int pos2 = positions[n + tables];
				CmdAddTable comAddTable = new CmdAddTable(r, j, array.get(pos1), array.get(pos2));
				comAddTable.execute();
			}
			rotateRR(positions);
		}
	}

	public List<Player> init(List<PlayerTourInfo> list) {
		ArrayList<Player> res = new ArrayList<Player>();
		for (PlayerTourInfo player : list) {
			if (player.isActive()) {
				res.add(player.getPlayer());
			}
		}
		if (res.size() % 2 != 0) {
			res.add(Player.DUMMY);
		}
		return res;
	}

	/**
	 * Cyclic shift right. For example 1 2 3 4 becomes 4 1 2 3 (for shift 1). 
	 * @param positions - array
	 * @param start - from index
	 * @param end - to index exclusive
	 * @param shift - number of shifts
	 */
	private void rotate(int[] positions, int start, int end, int shift) {
		if (positions == null)
			throw new NullPointerException();
		if (positions.length <= 2 || shift == 0)
			return;
		if (start < 0 || start >= positions.length)
			throw new ArrayIndexOutOfBoundsException(start);
		if (end <= 0 || end > positions.length)
			throw new ArrayIndexOutOfBoundsException(end);
		if (end <= start)
			throw new IllegalArgumentException("Start index should be less or equal end index: " + start + " <= " + end);
		if (shift < 0)
			throw new IllegalArgumentException("Shift cannot be negative");
		shift = shift % (end - start);
		while (shift-- > 0) {
			int x = positions[start];
			for (int i = start; i < end; i++) {
				int v = x;
				int j = i + 1;
				if (j == end)
					j = start;
				x = positions[j];
				positions[j] = v;
			}
		}
	}

	private void rotateRR(int[] positions) {
		// 0 . 1 2 
		// . 5 4 3
		// becomes
		// 0 . 5 1 
		// . 4 3 2
		int h = positions.length / 2;
		rotate(positions, 1, h, 1);
		rotate(positions, h, positions.length, h - 1);
		int x = positions[1];
		positions[1] = positions[positions.length - 1];
		positions[positions.length - 1] = x;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.mtgtournament.core.schedule.IScheduler#schedule(com.reflexit.mtgtournament.core.model.Round)
	 */
	public void schedule(Round r) {
		throw new IllegalStateException("Cannot schedule a single round for Round Robin");
	}
}
