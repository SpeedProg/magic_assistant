package com.reflexit.magiccards.core.model;

import com.reflexit.magiccards.core.model.storage.ILocatable;

public interface IMagicCardPhysical extends IMagicCard, ILocatable, ICardCountable {
	public String getComment();

	public Location getLocation();

	public boolean isOwn();

	public int getForTrade();

	public String getSpecial();

	public boolean isSideboard();

	public int getOwnCount();

	public int getOwnUnique();
}