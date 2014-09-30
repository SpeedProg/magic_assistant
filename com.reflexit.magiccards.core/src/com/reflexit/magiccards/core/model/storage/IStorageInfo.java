package com.reflexit.magiccards.core.model.storage;

public interface IStorageInfo {
	public static final String DECK_TYPE = "deck";
	public static final String COLLECTION_TYPE = "collection";

	public String getComment();

	public String getProperty(String key);

	public String getType();

	public void setComment(String text);

	public void setProperty(String key, String value);

	public void setType(String string);

	public void setVirtual(boolean value);

	public boolean isVirtual();

	public boolean isReadOnly();

	public String getName();

	public void setReadOnly(boolean value);
}
