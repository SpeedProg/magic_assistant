package com.reflexit.magiccards.core.model;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.expr.TextValue;

public interface IMagicCard extends ICard {
	public static final MagicCard DEFAULT = new MagicCard();
	public static final float STAR_POWER = 0.99F;
	public static final float NOT_APPLICABLE_POWER = Float.NaN;

	public abstract String getCost();

	public abstract int getCardId();

	public abstract int getGathererId();

	@Override
	public abstract String getName();

	public abstract String getOracleText();

	public abstract String getRarity();

	public abstract String getSet();

	public abstract String getType();

	public abstract String getPower();

	public abstract String getToughness();

	public abstract String getColorType();

	public abstract int getCmc();

	public abstract float getDbPrice();

	public abstract float getCommunityRating();

	public abstract String getArtist();

	public abstract String getRulings();

	@Override
	public IMagicCard cloneCard();

	public abstract MagicCard getBase();

	public abstract String getText();

	public abstract String getLanguage();

	public abstract boolean matches(ICardField left, TextValue right);

	public abstract int getEnglishCardId();

	public abstract int getFlipId();

	public int getUniqueCount();

	public abstract int getSide();

	public abstract int getCollectorNumberId();

	public LegalityMap getLegalityMap();

	public boolean isBasicLand();

	public String getEnglishName();

	public abstract Edition getEdition();
}