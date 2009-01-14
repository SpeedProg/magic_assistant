package com.reflexit.magiccards.core.model.storage;

public interface IMergeable<T> {
	public void setMergeOnAdd(boolean v);

	public boolean getMergeOnAdd();
}