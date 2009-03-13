package com.reflexit.magiccards.core.model.storage;

public interface IStorage<T> extends ICardSet<T>, ILocatable {
	public boolean isAutoCommit();

	public void setAutoCommit(boolean value);

	/**
	 * Save syncs memory cached data with physical media (from mem to physical).
	 * Save would called automatically after each data editing operation unless autoCommit is off.
	 */
	public void save();

	public boolean isNeedToBeSaved();

	/**
	 * Load syncs memory cashed data with physical media (from physical to mem). 
	 * Load would called automatically upon first data access if has not been loaded yet.
	 */
	public void load();

	public boolean isLoaded();

	public String getName();

	public String getComment();
}