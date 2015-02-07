package com.reflexit.mtgtournament.core.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.mtgtournament.core.model.Cube;
import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.tests.AbstractTournamentTest;
import com.reflexit.mtgtournament.core.tests.TestFileUtils;

import static org.junit.Assert.*;

public class XmlRegressionTest extends AbstractTournamentTest {
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws IOException {
		Cube cube = new Cube();
		File file = File.createTempFile("testTT", ".xml");
		TestFileUtils.saveResource("aaa.tour.xml", file);
		Object load = ModelLoader.load(file);
		assertTrue(load instanceof Tournament);
		Tournament aaa = (Tournament) load;
		List<PlayerTourInfo> playersInfo = aaa.getPlayersInfo();
		assertEquals(playersInfo.size(), 5);
		List<Round> rounds = aaa.getRounds();
		assertEquals(rounds.size(), 4);
		PlayerTourInfo pi = playersInfo.get(2);
		Set<Player> opponents = pi.getOpponents();
		boolean dummy = false;
		for (Player oo : opponents) {
			if (oo.isDummy())
				dummy = true;
		}
		assertTrue(dummy);
		Round round = rounds.get(2);
		TableInfo tableInfo = round.getTables().get(2);
		PlayerRoundInfo playerInfo1 = tableInfo.getOpponent(0);
		PlayerRoundInfo playerInfo = tableInfo.getOpponent(1);
		assertEquals("Player 3", playerInfo1.getPlayer().getName());
		assertTrue(playerInfo.getPlayer().isDummy());
		// update links
		aaa.setCube(cube);
		aaa.updateLinks();
	}

	@Test
	public void testDummy() throws Exception {
		Player d = Player.DUMMY;
		String saveToString = ModelLoader.saveToString(d);
		Player d2 = (Player) ModelLoader.load(saveToString);
		assertEquals(d, d2);
		assertTrue(d2.isDummy());
	}

	/*-
	<com.reflexit.mtgtournament.core.model.Player_-1>
	  <id>---</id>
	  <name>(dummy)</name>
	  <points>0</points>
	  <games>0</games>
	</com.reflexit.mtgtournament.core.model.Player_-1>
	*/
	@Test
	public void testDummyReg() throws Exception {
		Player d = Player.DUMMY;
		String saveToString = getAboveComment();
		Player d2 = (Player) ModelLoader.load(saveToString);
		assertEquals(d, d2);
		assertTrue(d2.isDummy());
	}
}
