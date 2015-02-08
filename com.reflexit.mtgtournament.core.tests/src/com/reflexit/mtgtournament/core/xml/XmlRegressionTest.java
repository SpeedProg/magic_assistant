package com.reflexit.mtgtournament.core.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

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

	/*-
	<player>
		<id>xxx1</id>
		<name>Alena Laskavaia</name>
		<note>note about me</note>
		<points>222</points>
		<games>33</games>
	</player>
	 */
	@Test
	public void testPlayer() throws Exception {
		Player d = newPlayerAlena();
		d.setNote("note about me");
		d.setGames(33);
		d.setPoints(222);
		//System.err.println(ModelLoader.saveToString(d));
		String saveToString = getAboveComment();
		Player d2 = (Player) ModelLoader.load(saveToString);
		assertEquals(d, d2);
		assertTrue(d.deepEquals(d2));
	}

	/*-
		<pRound>
		  <p>
		    <id>xxx1</id>
		    <name>Alena Laskavaia</name>
		    <points>0</points>
		    <games>0</games>
		  </p>
		  <gamesWon>-1</gamesWon>
		  <gamesDraw>0</gamesDraw>
		  <gamesLost>0</gamesLost>
		</pRound>
	*/
	@Test
	public void testPlayerInfo() throws Exception {
		Player d = newPlayerAlena();
		Round r = mock(Round.class);
		PlayerRoundInfo info = new PlayerRoundInfo(d, r);
		String saveToString = getAboveComment();
		PlayerRoundInfo info2 = (PlayerRoundInfo) ModelLoader.load(saveToString);
		Player d2 = info2.getPlayer();
		assertEquals(d, d2);
		assertTrue(d.deepEquals(d2));
		assertEquals(info.getWinStrDetails(), info2.getWinStrDetails());
	}

	/*-
	<pRound>
	  <p>
	    <id>xxx1</id>
	    <name>Alena Laskavaia</name>
	  </p>
	  <gamesWon>0</gamesWon>
	  <gamesDraw>3</gamesDraw>
	  <gamesLost>0</gamesLost>
	</pRound>
	*/
	@Test
	public void testPlayerInfo2() throws Exception {
		Player d = newPlayerAlena();
		Round r = mock(Round.class);
		PlayerRoundInfo info = new PlayerRoundInfo(d, r);
		info.setWinGames(0, 0, 3);
		String saveToString = getAboveComment();
		PlayerRoundInfo info2 = (PlayerRoundInfo) ModelLoader.load(saveToString);
		Player d2 = info2.getPlayer();
		assertEquals(d, d2);
		assertTrue(d.deepEquals(d2));
		assertEquals(info.getWinStrDetails(), info2.getWinStrDetails());
	}

	@Test
	public void testPlayerInfoDef() throws Exception {
		Player d = newPlayerAlena();
		PlayerRoundInfo info = new PlayerRoundInfo(d, mock(Round.class));
		assertEquals("_(_)", info.getWinStrDetails());
	}

	public Player newPlayerAlena() {
		return new Player("xxx1", "Alena Laskavaia");
	}

	/*-
	<list>
	<player>
	<id>xxx1</id>
	<name>Alena Laskavaia</name>
	<points>0</points>
	<games>0</games>
	</player>
	<pRound>
	<p reference="/list/player"/>
	<gamesWon>0</gamesWon>
	<gamesDraw>0</gamesDraw>
	<gamesLost>0</gamesLost>
	<r>_NONE</r>
	</pRound>
	</list>
	*/
	@Test
	public void testAbsoluteLinks() throws Exception {
		Player d = newPlayerAlena();
		PlayerRoundInfo info = new PlayerRoundInfo(d, mock(Round.class));
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(d);
		list.add(info);
		String saveToString = getAboveComment();
		List list1 = (List) ModelLoader.load(saveToString);
		PlayerRoundInfo info2 = (PlayerRoundInfo) list1.get(1);
		Player d2 = info2.getPlayer();
		assertEquals(d, d2);
		assertTrue(d.deepEquals(d2));
		assertEquals(info.getWinStrDetails(), info2.getWinStrDetails());
		assertTrue(info.deepEquals(info2));
	}

	/*-
	 <list>
	<player>
	<id>xxx1</id>
	<name>Alena Laskavaia</name>
	<points>1</points>
	<games>0</games>
	</player>
	<pRound>
	<p reference="../../player"/>
	<gamesWon>0</gamesWon>
	<gamesDraw>0</gamesDraw>
	<gamesLost>0</gamesLost>
	<r>_NONE</r>
	</pRound>
	</list>

	 */
	public void testRelativeLinks() throws Exception {
		Player d = newPlayerAlena();
		d.setPoints(1);
		PlayerRoundInfo info = new PlayerRoundInfo(d, mock(Round.class));
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(d);
		list.add(info);
		String saveToString = getAboveComment();
		List list1 = (List) ModelLoader.load(saveToString);
		PlayerRoundInfo info2 = (PlayerRoundInfo) list1.get(1);
		Player d2 = info2.getPlayer();
		assertEquals(d, d2);
		assertTrue(d.deepEquals(d2));
		assertEquals(info.getWinStrDetails(), info2.getWinStrDetails());
		assertTrue(info.deepEquals(info2));
	}
}
