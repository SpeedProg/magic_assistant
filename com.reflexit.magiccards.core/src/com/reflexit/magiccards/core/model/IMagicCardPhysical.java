package com.reflexit.magiccards.core.model;

import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ILocatable;

public interface IMagicCardPhysical extends IMagicCard, ILocatable, ICardCountable {
	public String getComment();

	@Override
	public Location getLocation();

	public boolean isOwn();

	public int getForTrade();

	public String getSpecial();

	public boolean isSideboard();

	public int getOwnCount();

	public int getOwnUnique();

	public int getOwnTotalAll();

	public float getPrice();

	/**
	 * Return true of card is physical (virtual or own) or group of cards has only physycal cards
	 * 
	 * @return
	 */
	public boolean isPhysical();
}