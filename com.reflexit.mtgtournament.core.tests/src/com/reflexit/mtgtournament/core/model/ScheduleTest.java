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
				Player p1 = tableInfo.getPlayerInfo(1).getPlayer();
				Player p2 = tableInfo.getPlayerInfo(2).getPlayer();
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
		tour.setType(TournamentType.SWISS, 5, true);
		tour.generatePlayers(6);
		scheduleAll(tour);
	}

	/**
	 * @param r
	 */
	private void generateWinnigs(Round r) {
		Random ra = new Random();
		for (TableInfo ti : r.getTables()) {
			int pw = ra.nextInt(2);
			PlayerRoundInfo p1 = ti.getPlayerInfo(1);
			if (p1.getPlayer() == Player.DUMMY) {
				pw = 1;
			}
			PlayerRoundInfo p2 = ti.getPlayerInfo(2);
			if (p2.getPlayer() == Player.DUMMY) {
				pw = 0;
			}
			if (pw == 0) {
				p1.setResult(PlayerGameResult.WIN);
				p2.setResult(PlayerGameResult.LOOSE);
				p1.setWinGames(1);
			} else {
				p2.setResult(PlayerGameResult.WIN);
				p1.setResult(PlayerGameResult.LOOSE);
				p2.setWinGames(1);
			}
		}
	}

	public void testSwiss_odd() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.SWISS, 6, true);
		tour.generatePlayers(7);
		scheduleAll(tour);
	}

	public void testSwiss_More() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.SWISS, 6, true);
		tour.generatePlayers(30);
		scheduleAll(tour);
	}

	public void testElimination_8() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.ELIMINATION, 3, true);
		tour.generatePlayers(8);
		scheduleAll(tour);
	}

	private void scheduleAll(Tournament tour) {
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

	public void testElimination_Opt8() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.ELIMINATION, 0, true);
		tour.generatePlayers(8);
		tour.schedule();
		assertEquals(3, tour.getNumberOfRounds());
	}

	public void testElimination_Opt7() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.ELIMINATION, 0, true);
		tour.generatePlayers(7);
		tour.schedule();
		assertEquals(3, tour.getNumberOfRounds());
	}

	public void testElimination_Opt9() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.ELIMINATION, 0, true);
		tour.generatePlayers(9);
		tour.schedule();
		assertEquals(4, tour.getNumberOfRounds());
	}

	public void testElimination_10() {
		Tournament tour = new Tournament();
		tour.setType(TournamentType.ELIMINATION, 3, true);
		tour.generatePlayers(10);
		scheduleAll(tour);
	}
}
