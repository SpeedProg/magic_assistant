package com.reflexit.magiccards.core.model.storage;

public interface IStorageInfo {
	public String getComment();

	public String getProperty(String key);

	public String getType();

	public void setComment(String text);
}
