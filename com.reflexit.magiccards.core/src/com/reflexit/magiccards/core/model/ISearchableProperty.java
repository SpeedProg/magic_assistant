package com.reflexit.magiccards.core.model;

import java.util.Collection;

public interface ISearchableProperty {
	public String getIdPrefix();

	public FilterField getFilterField();

	public Collection<String> getIds();

	public String getNameById(String id);
}