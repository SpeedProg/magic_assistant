package com.reflexit.magiccards.ui.views;

public interface IMagicControl extends IViewPage {
	public abstract void refresh();

	public abstract void updateViewer();

	public abstract void reloadData();
}