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
package com.reflexit.mtgtournament.core.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tournament {
	private transient Cube cube;
	private String name;
	private Date dateStart;
	private Date dateEnd;
	private String comment;
	private List<PlayerTourInfo> players = new ArrayList<PlayerTourInfo>();
	private List<Round> rounds = new ArrayList<Round>();
	private int numberOfRounds;
	private TournamentType type;
	private boolean draftRound;
	private boolean scheduled;
	private boolean closed;

	public Tournament() {
	}

	public Tournament(String name) {
		this.name = name;
		this.type = TournamentType.ROUND_ROBIN;
		this.numberOfRounds = 0;
		this.draftRound = true;
	}

	public void updateLinks() {
		int i = 0;
		for (Round r : rounds) {
			r.setTournament(this);
			r.setNumber(i);
			r.updateLinks();
			i++;
		}
		for (PlayerTourInfo pt : players) {
			pt.setTournament(this);
			Player np = cube.getPlayerList().findPlayer(pt.getPlayer());
			if (np != null)
				pt.setPlayer(np);
		}
	}

	/**
	 * 
	 * @param type -type of the scheduler, @see {@link TournamentType}
	 * @param rounds - number of round, 0 if optimal (max for RR)
	 * @param draft - is there draft round
	 */
	public void setType(TournamentType type) {
		if (isScheduled() && this.type != type)
			throw new IllegalStateException("Cannot modify type when tournament is already scheduled");
		this.type = type;
	}

	/**
	 * 
	 * @param type -type of the scheduler, @see {@link TournamentType}
	 * @param rounds - number of round, 0 if optimal (max for RR)
	 * @param draft - is there draft round
	 */
	public void setNumberOfRounds(int rounds, boolean draft) {
		if (isScheduled() && (this.draftRound != draft || this.numberOfRounds != rounds))
			throw new IllegalStateException("Cannot modify type when tournament is already scheduled");
		this.draftRound = draft;
		this.setNumberOfRounds(rounds);
	}

	public void generatePlayers(int num) {
		for (int i = 0; i < num; i++) {
			addPlayer(new Player("f" + i, "Player " + (i + 1)));
		}
	}

	public void schedule() {
		if (getPlayersInfo().size() < 2) {
			throw new IllegalStateException("Not enought players");
		}
		if (isScheduled())
			throw new IllegalStateException("Cannot schedule - tournament is already scheduled");
		rounds.clear();
		getType().getScheduler().schedule(this);
		this.setScheduled(true);
	}

	/**
	 * @param round
	 */
	public void schedule(Round round) {
		round.getType().getScheduler().schedule(round);
	}

	public Round getRound(int i) {
		return rounds.get(i);
	}

	public void printSchedule(PrintStream st) {
		for (Round round : rounds) {
			if (round != null) {
				if (round.getNumber() == 0) {
					if (isDraftRound())
						st.println("Draft" + ": ");
				} else
					st.println("Round " + round.getNumber() + ": ");
				round.printSchedule(st);
			}
		}
	}

	public static boolean hasPlayed(Player p1, Player p2, Round r) {
		List<TableInfo> tables = r.getTables();
		for (Object element : tables) {
			TableInfo tableInfo = (TableInfo) element;
			int count = 0;
			for (int i = 0; i < tableInfo.getPlayerRoundInfo().length; i++) {
				PlayerRoundInfo pi = tableInfo.getPlayerRoundInfo()[i];
				if (pi.getPlayer() == p1)
					count++;
				else if (pi.getPlayer() == p2)
					count++;
			}
			if (count == 2)
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

	public List<PlayerTourInfo> getPlayersInfo() {
		return players;
	}

	public void setNumberOfRounds(int numberOfRounds) {
		this.numberOfRounds = numberOfRounds;
	}

	public int getNumberOfRounds() {
		return numberOfRounds;
	}

	public void addRound(Round r) {
		r.setTournament(this);
		rounds.add(r);
	}

	/**
	 * @param round
	 * @return
	 */
	public boolean removeRound(Round round) {
		return rounds.remove(round);
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

	public void removePlayer(PlayerTourInfo object) {
		players.remove(object);
	}

	public boolean addPlayer(Player player) {
		PlayerTourInfo playerTourInfo = new PlayerTourInfo(player);
		if (players.contains(playerTourInfo))
			return false;
		players.add(playerTourInfo);
		playerTourInfo.setTournament(this);
		return true;
	}

	public void setCube(Cube cube) {
		this.cube = cube;
	}

	public Cube getCube() {
		return cube;
	}

	public TournamentType getType() {
		return type;
	}

	/**
	 * @param scheduled - the scheduled to set
	 */
	public void setScheduled(boolean scheduled) {
		this.scheduled = scheduled;
		if (scheduled == false)
			rounds.clear();
	}

	/**
	 * @return the scheduled
	 */
	public boolean isScheduled() {
		return scheduled;
	}

	/**
	 * @param player
	 */
	public void playerDropped(Player player) {
		PlayerTourInfo info = findPlayerTourInfo(player);
		if (info != null)
			info.setActive(false);
	}

	/**
	 * @param player
	 * @return
	 */
	public PlayerTourInfo findPlayerTourInfo(Player player) {
		for (PlayerTourInfo pi : players) {
			if (pi.getPlayer().equals(player))
				return pi;
		}
		return null;
	}

	public static int comparePlayers(PlayerTourInfo a, PlayerTourInfo b) {
		if (a.getPoints() != b.getPoints())
			return b.getPoints() - a.getPoints();
		if (a.getGames() != b.getGames())
			return a.getGames() - b.getGames();
		if (a.getWin() != b.getWin())
			return b.getWin() - a.getWin();
		return 0;
	}

	/**
	 * @return
	 */
	public int getNumberOfPlayers() {
		return players.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Tournament))
			return false;
		Tournament other = (Tournament) obj;
		if (name == null) {
			if (other.name != null)
				return false;
			return super.equals(obj);
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * @return
	 */
	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}
}
