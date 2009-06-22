package com.reflexit.mtgtournament.core.model;

import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo.PlayerGameResult;

public class ScheduleTest extends TestCase {
	private void checkPlayedBefore(Tournament tour, int tP) {
		int n = tour.getNumberOfRounds();
		for (int i = 1; i < n; i++) {
			Round r = tour.getRound(i);
			List<TableInfo> tables = r.getTables();
			for (Object element : tables) {
				TableInfo tableInfo = (TableInfo) element;
				Player p1 = tableInfo.p1.p;
				Player p2 = tableInfo.p2.p;
				boolean has = tour.hasPlayed(p1, p2, i - 1);
				if (has) {
					if (i != tP)
						assertFalse("Players " + p1 + " and " + p2 + " has played in round before " + i, has);
					else
						return;
				}
			}
		}
		if (tP >= 0)
			fail("Cannot find tour");
	}

	public void testRoundRoubin() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.ROUND_ROBIN, 0, true);
		tour.generatePlayers(6);
		tour.schedule();
		assertEquals(5, tour.getNumberOfRounds());
		//tour.printSchedule(System.out);
		checkPlayedBefore(tour, -1);
	}

	public void testRoundRoubin_MoreRounds() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.ROUND_ROBIN, 4, true);
		tour.generatePlayers(4);
		tour.schedule();
		//tour.printSchedule(System.out);
		checkPlayedBefore(tour, 2);
	}

	public void testSwiss() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.SWISS, 3, true);
		tour.generatePlayers(6);
		tour.schedule();
		for (int i = 0; i <= tour.getNumberOfRounds(); i++) {
			Round r = tour.getRound(i);
			r.schedule();
			if (i > 0) {
				generateWinnigs(r);
			}
			r.close();
		}
		tour.printSchedule(System.out);
		checkPlayedBefore(tour, -1);
	}

	/**
	 * @param r
	 */
	private void generateWinnigs(Round r) {
		Random ra = new Random();
		for (TableInfo ti : r.getTables()) {
			int pw = ra.nextInt(2);
			if (ti.getP1().getPlayer() == Player.DUMMY) {
				pw = 1;
			}
			if (ti.getP2().getPlayer() == Player.DUMMY) {
				pw = 0;
			}
			if (pw == 0) {
				ti.getP1().setResult(PlayerGameResult.WIN);
				ti.getP2().setResult(PlayerGameResult.LOOSE);
				ti.getP1().setWinGames(1);
			} else {
				ti.getP2().setResult(PlayerGameResult.WIN);
				ti.getP1().setResult(PlayerGameResult.LOOSE);
				ti.getP2().setWinGames(1);
			}
		}
	}
}
