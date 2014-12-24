package com.reflexit.magiccards.core.model;

import java.util.List;

public interface ICardGroup extends ICard {
	public abstract List<? extends ICard> getChildrenList();

	public abstract ICard[] getChildren();

	public abstract int size();

	public abstract void add(ICard elem);

	public abstract void remove(ICard elem);

	@Override
	public abstract String getName();

	public abstract ICardField getFieldIndex();

	public abstract ICard getChildAtIndex(int index);

	public abstract ICardGroup getSubGroup(String key);
}