package com.reflexit.magiccards.core.model;

import java.util.Collection;

public interface ISearchableProperty {
	public String getIdPrefix();

	public Collection getNames();

	public Collection getIds();

	public String getNameById(String id);
}