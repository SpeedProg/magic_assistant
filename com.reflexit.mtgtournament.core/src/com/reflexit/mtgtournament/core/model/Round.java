package com.reflexit.mtgtournament.core.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Round {
	transient Tournament tournament;
	transient int number;
	transient List<PlayerRoundInfo> playerInfo = new ArrayList<PlayerRoundInfo>();
	List<TableInfo> tables = new ArrayList<TableInfo>();

	public Round(int number) {
		this.number = number;
	}

	public void init(List<Player> players) {
		for (Object element : players) {
			Player player = (Player) element;
			PlayerRoundInfo info = new PlayerRoundInfo(player, this);
			playerInfo.add(info);
		}
		if (players.size() % 2 != 0) {
			PlayerRoundInfo info = new PlayerRoundInfo(Player.DUMMY, this);
			playerInfo.add(info);
		}
	}

	public int getPlayersNumber() {
		return playerInfo.size();
	}

	public PlayerRoundInfo getPlayerInfo(int j) {
		if (j < 0 || j >= getPlayersNumber())
			return null;
		return playerInfo.get(j);
	}

	public void addTable(TableInfo t) {
		tables.add(t);
	}

	@Override
	public String toString() {
		return tables.toString();
	}

	public void printSchedule(PrintStream st) {
		for (Object element : tables) {
			TableInfo table = (TableInfo) element;
			st.println("Table " + table.table + ": " + table.p1.p + " vs " + table.p2.p);
		}
	}

	public List<TableInfo> getTables() {
		return tables;
	}

	public int getNumber() {
		return number;
	}
}
