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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.reflexit.mtgtournament.core.edit.CmdCommitRounds;

public class Round implements Cloneable {
	private transient Tournament tournament;
	private transient int number;
	private TournamentType type;
	private List<TableInfo> tables = new ArrayList<TableInfo>();
	private Date dateStart;
	private Date dateEnd;

	public Round(int number) {
		this.number = number;
	}

	public boolean isScheduled() {
		return tables.size() > 0;
	}

	public PlayerRoundInfo makeDummy() {
		PlayerRoundInfo info = new PlayerRoundInfo(Player.DUMMY, this);
		return info;
	}

	public PlayerRoundInfo makePlayer(Player player) {
		PlayerRoundInfo info = new PlayerRoundInfo(player, this);
		return info;
	}

	public void addTable(TableInfo t) {
		tables.add(t);
		t.setRound(this);
	}

	@Override
	public String toString() {
		return tables.toString();
	}

	public RoundState getState() {
		Round round = this;
		if (round.getNumber() > 0) {
			Round prev = round.getTournament().getRound(round.getNumber() - 1);
			if (prev != null) {
				if (prev.getState() != RoundState.CLOSED)
					return RoundState.NOT_READY;
			}
		}
		if (!round.isScheduled())
			return RoundState.NOT_SCHEDULED;
		if (round.getDateStart() == null)
			return RoundState.READY;
		if (round.getDateEnd() == null)
			return RoundState.IN_PROGRESS;
		return RoundState.CLOSED;
	}

	public void printSchedule(PrintStream st) {
		for (Object element : tables) {
			TableInfo table = (TableInfo) element;
			st.println("Table " + table.getTableNumber() + ": " + table.getP1().p + " vs " + table.getP2().p);
		}
	}

	public List<TableInfo> getTables() {
		return tables;
	}

	public int getNumber() {
		return number;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(TournamentType type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public TournamentType getType() {
		return type;
	}

	/**
	 * @param tournament the tournament to set
	 */
	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}

	/**
	 * @return the tournament
	 */
	public Tournament getTournament() {
		return tournament;
	}

	/**
	 * @param dateStart the dateStart to set
	 */
	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}

	/**
	 * @return the dateStart
	 */
	public Date getDateStart() {
		return dateStart;
	}

	/**
	 * @param dateEnd the dateEnd to set
	 */
	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}

	/**
	 * @return the dateEnd
	 */
	public Date getDateEnd() {
		return dateEnd;
	}

	public void schedule() {
		getTournament().schedule(this);
	}

	/**
	 * 
	 */
	public void close() {
		setDateEnd(Calendar.getInstance().getTime());
		if (dateStart == null)
			dateStart = dateEnd;
		new CmdCommitRounds(tournament, getNumber()).execute();
	}

	/**
	 * 
	 */
	public void updateLinks() {
		for (TableInfo t : tables) {
			t.setRound(this);
		}
	}

	/**
	 * @param i
	 */
	public void setNumber(int i) {
		this.number = i;
	}

	@Override
	public Object clone() {
		Round r;
		try {
			r = (Round) super.clone();
			r.tables = (List<TableInfo>) ((ArrayList<TableInfo>) this.tables).clone();
			return r;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * @param roundNew
	 */
	public void copyFrom(Round roundNew) {
		this.tournament = roundNew.tournament;
		this.number = roundNew.number;
		this.type = roundNew.type;
		this.tables = (List<TableInfo>) ((ArrayList<TableInfo>) roundNew.tables).clone();
		this.dateStart = roundNew.dateStart;
		this.dateEnd = roundNew.dateEnd;
	}
}
