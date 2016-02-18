package com.reflexit.mtgtournament.core.edit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.TableInfo;

public class CmdChangePairing implements ITCommand {
	private TableInfo table;
	private Player newpartner;
	private Player oldpartner;

	public CmdChangePairing(TableInfo table, Player player2) {
		super();
		this.table = table;
		this.newpartner = player2;
		this.oldpartner = table.getOpponent(table.getOpponentsPerGame() - 1).getPlayer();
	}

	public boolean execute() {
		if (newpartner == oldpartner || newpartner == null)
			return false;
		int opp = table.getOpponentsPerGame();
		if (!newpartner.isDummy()) {
			// find where newpartner was before
			List<TableInfo> tables = table.getRound().getTables();
			for (TableInfo tableInfo : tables) {
				PlayerRoundInfo[] playerRoundInfo = tableInfo.getPlayerRoundInfo();
				for (int i = 0; i < playerRoundInfo.length; i++) {
					PlayerRoundInfo pri = playerRoundInfo[i];
					if (pri.getPlayer() == newpartner) {
						pri.setPlayer(oldpartner);
						pri.setWinGames(0, 0, 0);
						break;
					}
				}
			}
		} else {
			ArrayList<Player> players = new ArrayList<Player>();
			players.add(oldpartner);
			while (players.size() % opp != 0) {
				players.add(Player.DUMMY);
			}
			Player[] newpl = players.toArray(new Player[players.size()]);
			new CmdAddTable(table.getRound(), newpl).execute();
		}
		// shift everybody by 1 to the right
		for (int i = 1; i < opp - 1; i++) {
			table.getOpponent(i + 1).setPlayer(table.getOpponent(i).getPlayer());
		}
		table.getOpponent(1).setPlayer(newpartner);
		deleteDummyPairing();
		return true;
	}

	private void deleteDummyPairing() {
		List<TableInfo> tables = table.getRound().getTables();
		for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
			TableInfo tableInfo = (TableInfo) iterator.next();
			PlayerRoundInfo[] playerRoundInfo = tableInfo.getPlayerRoundInfo();
			int dum = 0;
			for (int i = 0; i < playerRoundInfo.length; i++) {
				PlayerRoundInfo pri = playerRoundInfo[i];
				if (pri.getPlayer().isDummy()) {
					dum++;
				}
			}
			// System.err.println("Found " + dum + " dummies of " +
			// playerRoundInfo.length + " at table " +
			// tableInfo.getTableNumber() + " "
			// + tableInfo.getRound().hashCode());
			if (dum == playerRoundInfo.length) {
				new CmdDeleteTable(tableInfo).execute();
				break;
			}
		}
	}

	public boolean undo() {
		return new CmdChangePairing(table, oldpartner).execute();
	}
}
