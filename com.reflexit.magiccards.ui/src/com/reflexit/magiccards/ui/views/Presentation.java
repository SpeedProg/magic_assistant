package com.reflexit.magiccards.ui.views;

public enum Presentation {
	TABLE, TREE, SPLITTREE, GALLERY;
	public String getLabel() {
		return name();
	}
}