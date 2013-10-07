package com.reflexit.magiccards.core.legality;

public class ConstructedFormat extends Format {
	public ConstructedFormat(String name) {
		super(name);
	}

	protected ConstructedFormat(String name, int ord) {
		super(name, ord);
	}

	@Override
	public int getMainDeckCount() {
		return 60;
	}

	@Override
	public String validateCardCount(int count) {
		if (count <= 4)
			return null;
		return "Card count is " + count + " expected <= 4";
	}
}
