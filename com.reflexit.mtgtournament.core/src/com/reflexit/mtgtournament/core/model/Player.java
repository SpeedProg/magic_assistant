package com.reflexit.mtgtournament.core.model;

public class Player {
	public static final Player DUMMY = new Player("---", "---");
	private String id;
	private String name;
	private String note;
	private int rank;

	public Player(String id, String name) {
		this.id = id;
		this.name = name;
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
}
