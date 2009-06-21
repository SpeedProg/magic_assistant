package com.reflexit.mtgtournament.core.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.reflexit.mtgtournament.core.schedule.RoundRobinSchedule;

public class Tournament {
	String name;
	Date dateStart;
	Date dateEnd;
	String comment;
	private List<Player> players = new ArrayList<Player>();
	List<Round> rounds = new ArrayList<Round>();
	private int numberOfRounds;
	TournamentType type;
	private boolean draftRound;
	transient Cube cube;
	static public enum TournamentType {
		ROUND_ROBIN,
		SWISS,
		ELIMINATION
	}

	public Tournament() {
	}

	/**
	 * 
	 * @param type -type of the scheduler, @see {@link TournamentType}
	 * @param rounds - number of round, 0 if optimal (max for RR)
	 * @param draft - is there draft round
	 */
	public void setType(TournamentType type, int rounds, boolean draft) {
		this.type = type;
		this.draftRound = draft;
		this.setNumberOfRounds(rounds);
	}

	public void generatePlayers(int num) {
		players.clear();
		for (int i = 0; i < num; i++) {
			getPlayers().add(new Player("f" + i, "Player " + (i + 1)));
		}
	}

	public void schedule() {
		if (type == TournamentType.ROUND_ROBIN) {
			new RoundRobinSchedule().schedule(this);
		}
	}

	public Round getRound(int i) {
		return rounds.get(i);
	}

	public void printSchedule(PrintStream st) {
		for (Round round : rounds) {
			if (round != null) {
				if (round.number == 0)
					st.println("Draft" + ": ");
				else
					st.println("Round " + round.number + ": ");
				round.printSchedule(st);
			}
		}
	}

	public static boolean hasPlayed(Player p1, Player p2, Round r) {
		List<TableInfo> tables = r.getTables();
		for (Object element : tables) {
			TableInfo tableInfo = (TableInfo) element;
			if (tableInfo.p1.p == p1 && tableInfo.p2.p == p2)
				return true;
			if (tableInfo.p1.p == p2 && tableInfo.p2.p == p1)
				return true;
		}
		return false;
	}

	public boolean hasPlayed(Player p1, Player p2, int upToRound) {
		if (upToRound == -1)
			upToRound = (rounds.size() - 1);
		for (int i = 1; i <= upToRound; i++) {
			Round r = rounds.get(i);
			boolean has = hasPlayed(p1, p2, r);
			if (has)
				return true;
		}
		return false;
	}

	public boolean isDraftRound() {
		return draftRound;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setNumberOfRounds(int numberOfRounds) {
		this.numberOfRounds = numberOfRounds;
	}

	public int getNumberOfRounds() {
		return numberOfRounds;
	}

	public void addRound(Round r) {
		r.tournament = this;
		rounds.add(r);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public List<Round> getRounds() {
		return rounds;
	}

	public void removePlayer(Player object) {
		players.remove(object);
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public void setCube(Cube cube) {
		this.cube = cube;
	}

	public Cube getCube() {
		return cube;
	}
}
