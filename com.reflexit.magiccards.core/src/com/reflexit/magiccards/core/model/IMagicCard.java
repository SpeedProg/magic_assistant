package com.reflexit.magiccards.core.model;

public interface IMagicCard extends ICard {
	public static final MagicCard DEFAULT = new MagicCard();
	public static final float STAR_POWER = 911.0F;
	public static final float NOT_APPLICABLE_POWER = Float.NaN;

	public abstract String getCost();

	public abstract int getCardId();

	public abstract String getName();

	public abstract String getOracleText();

	public abstract String getRarity();

	public abstract String getSet();

	public abstract String getType();

	public abstract String getPower();

	public abstract String getToughness();

	public abstract String getColorType();

	public abstract int getCmc();

	//	public abstract String getByIndex(int i);
	//
	//	public abstract Object getObjectByIndex(int i);
	public abstract float getDbPrice();
}