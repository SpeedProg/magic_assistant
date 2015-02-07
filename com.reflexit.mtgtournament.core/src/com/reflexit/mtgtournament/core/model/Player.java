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

public class Player {
	public static final Player DUMMY = new Player("---", "(dummy)") {
		@Override
		public boolean isDummy() {
			return true;
		};
	};
	private String id;
	private String name;
	private String note;
	private int points = 0;
	private int games = 0;

	public boolean isDummy() {
		return false;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Player(String id, String name) {
		this.id = id;
		this.name = name;
		if (!isDummy()) {
			if (equals(DUMMY))
				throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		if (id != null) return id.hashCode();
		return 31 + ((name == null) ? 0 : name.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Player))
			return false;
		Player other = (Player) obj;
		if (id == null) {
			if (other.id != null)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public boolean deepEquals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Player)) return false;
		Player other = (Player) obj;
		if (games != other.games) return false;
		if (id == null) {
			if (other.id != null) return false;
		} else if (!id.equals(other.id)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (note == null) {
			if (other.note != null) return false;
		} else if (!note.equals(other.note)) return false;
		if (points != other.points) return false;
		return true;
	}

	public int getPoints() {
		return points;
	}

	public int getGames() {
		return games;
	}

	/**
	 * @param text
	 */
	public void setName(String text) {
		if (text.equals(DUMMY.name))
			throw new IllegalArgumentException();
		name = text;
	}

	/**
	 * @param text
	 */
	public void setId(String text) {
		if (text.equals(DUMMY.id))
			throw new IllegalArgumentException();
		this.id = text;
	}

	/**
	 * @param games
	 *            the games to set
	 */
	public void setGames(int games) {
		this.games = games;
	}

	/**
	 * @param points
	 *            the points to set
	 */
	public void setPoints(int points) {
		this.points = points;
	}
}
