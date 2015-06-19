package com.reflexit.magiccards.core.model.storage;

import com.reflexit.magiccards.core.model.abs.ICardSet;

public interface IStorage<T> extends ICardSet<T>, ILocatable {
	public boolean isAutoCommit();

	public void setAutoCommit(boolean value);

	/**
	 * Syncs cached data with physical media (from mem to physical).
	 * Save would called automatically after each data editing operation unless autoCommit is off.
	 */
	public void save();

	public boolean isNeedToBeSaved();

	/**
	 * Initiate a save command. It will result is actual save if auto-commit is on. If it is off
	 * saving will be postponed.
	 */
	public void autoSave();

	/**
	 * Load syncs memory cashed data with physical media (from physical to mem). Load would called
	 * automatically upon first data access if has not been loaded yet.
	 */
	public void load();

	public boolean isLoaded();

	public String getName();

	public String getComment();

	public boolean isVirtual();
}