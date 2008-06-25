package com.reflexit.magiccards.core.model;

public interface IMagicCard {
	public static final MagicCard DEFAULT = new MagicCard();
	public static final float STAR_POWER = 911.0F;
	public static final float NOT_APPLICABLE_POWER = Float.NaN;
	public static final int INDEX_ID = 0;
	public static final int INDEX_NAME = 1;
	public static final int INDEX_COST = 2;
	public static final int INDEX_TYPE = 3;
	public static final int INDEX_POWER = 4;
	public static final int INDEX_TOUGHNESS = 5;
	public static final int INDEX_ORACLE = 6;
	public static final int INDEX_EDITION = 7;
	public static final int INDEX_RARITY = 8;
	public static final int INDEX_CTYPE = 9;
	public static final int INDEX_CMC = 10;

	public abstract String getCost();

	public abstract int getCardId();

	public abstract String getName();

	public abstract String getOracleText();

	public abstract String getRarity();

	public abstract String getEdition();

	public abstract String getType();

	public abstract String getPower();

	public abstract String getToughness();

	public abstract String getColorType();

	public abstract int getCmc();

	public abstract String getByIndex(int i);

	public abstract Object getObjectByIndex(int i);
}