package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ColumnViewer;

import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public interface IMagicColumnViewer extends IMagicViewer {
	public abstract ColumnCollection getColumnsCollection();

	public abstract void updateColumns(String preferenceValue);

	public abstract void setSortColumn(int index, int direction);

	ColumnViewer getColumnViewer();

	public abstract int getSortDirection();

	public abstract String getColumnLayoutProperty();
}