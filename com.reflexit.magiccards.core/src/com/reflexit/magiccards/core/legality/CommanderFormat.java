package com.reflexit.magiccards.core.legality;

public class CommanderFormat extends Format {
	public CommanderFormat() {
		super("Commander", 9);
	}

	@Override
	public int getMainDeckCount() {
		return 99;
	}

	@Override
	public int getSideboardCount() {
		return 1;
	}

	@Override
	public String validateDeckCount(int count) {
		int min = getMainDeckCount();
		if (count == min)
			return null;
		return "Deck card count is " + count + " expected to be exactly " + min
				+ ". Commander should be in sideboard.";
	}

	@Override
	public String validateSideboardCount(int count) {
		int max = getSideboardCount();
		if (count == max)
			return null;
		return "Sideboard should contain one commander card";
	}

	@Override
	public String validateCardCount(int count) {
		if (count <= 1)
			return null;
		return "Singleton. Only one card with this name allowed";
	}
}
