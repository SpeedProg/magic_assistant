package com.reflexit.mtgtournament.core.schedule;

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;

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
public class RoundRobinSchedule {
	public void schedule(Tournament t) {
		int pn = t.getPlayersInfo().size();
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
		for (int i = 0; i <= t.getNumberOfRounds(); i++) {
			if ((!t.isDraftRound()) && i == 0) {
				t.addRound(null);
				continue;
			}
			Round r = new Round(i);
			t.addRound(r);
			r.init(t.getPlayersInfo());
			for (int j = 0; j < tables; j++) {
				int n = (j + i) % tables;
				PlayerRoundInfo p1 = r.getPlayerInfo(positions[n]);
				PlayerRoundInfo p2 = r.getPlayerInfo(positions[n + tables]);
				TableInfo tableInfo = new TableInfo(j, r, p1, p2);
				r.addTable(tableInfo);
			}
			rotateRR(positions);
		}
	}

	private void rotate(int[] positions, int start, int end, int shift) {
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
		// 0 . 1 2 . 5 4 3
		// becomes
		// 0 . 5 1 . 4 3 2
		int h = positions.length / 2;
		rotate(positions, 1, h, 1);
		rotate(positions, 3, positions.length, h - 1);
		int x = positions[1];
		positions[1] = positions[positions.length - 1];
		positions[positions.length - 1] = x;
	}
}
