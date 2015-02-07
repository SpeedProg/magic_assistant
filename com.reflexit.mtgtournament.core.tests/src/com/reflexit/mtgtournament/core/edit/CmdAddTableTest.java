package com.reflexit.mtgtournament.core.edit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.reflexit.mtgtournament.core.edit.CmdAddTable;
import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.Tournament;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.JVM)
public class CmdAddTableTest {
	private Tournament tour;
	private List<PlayerTourInfo> playersInfo;
	private List<Player> players;
	private Round round;

	@Before
	public void setUp() throws Exception {
		tour = new Tournament();
		tour.generatePlayers(6);
		playersInfo = tour.getPlayersInfo();
		players = new ArrayList<Player>();
		for (PlayerTourInfo playerInfo : playersInfo) {
			players.add(playerInfo.getPlayer());
		}
		round = new Round(1);
		round.setTournament(tour);
	}

	@Test
	public void testCmdAddTable() {
		CmdAddTable comm = new CmdAddTable(round, players.get(0), players.get(1));
		assertEquals(round, comm.getRound());
		assertEquals(2, comm.getTableInfo().getOpponentsPerGame());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCmdAddTable1() {
		CmdAddTable comm = new CmdAddTable(round, players.get(0));
	}

	@Test
	public void testCmdAddTable3() {
		CmdAddTable comm = new CmdAddTable(round, players.get(0), players.get(1), players.get(2));
		assertEquals(3, comm.getTableInfo().getOpponentsPerGame());
	}

	@Test
	public void testExecute() {
		CmdAddTable comm = new CmdAddTable(round, players.get(0), players.get(1), players.get(2));
		comm.execute();
		assertTrue(comm.getRound().getTables().contains(comm.getTableInfo()));
	}

	@Test
	public void testUndo() {
		CmdAddTable comm = new CmdAddTable(round, players.get(0), players.get(1));
		comm.execute();
		assertTrue(comm.getRound().getTables().contains(comm.getTableInfo()));
		comm.undo();
		assertFalse(comm.getRound().getTables().contains(comm.getTableInfo()));
		assertEquals(0, comm.getRound().getTables().size());
	}

	@Test
	public void testExecute2() {
		CmdAddTable comm = new CmdAddTable(round, players.get(0), players.get(1));
		comm.execute();
		CmdAddTable comm1 = new CmdAddTable(round, players.get(2), players.get(3));
		comm1.execute();
		assertTrue(round.getTables().contains(comm.getTableInfo()));
		assertEquals(1, comm.getTableInfo().getTableNumber());
		assertEquals(2, comm1.getTableInfo().getTableNumber());
	}
}
