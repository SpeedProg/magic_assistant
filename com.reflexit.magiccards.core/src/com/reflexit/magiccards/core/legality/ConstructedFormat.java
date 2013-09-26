package com.reflexit.magiccards.core.legality;

public class ConstructedFormat extends Format {
	public ConstructedFormat(String name) {
		super(name);
	}

	@Override
	public int getMainDeckCount() {
		return 60;
	}
}
