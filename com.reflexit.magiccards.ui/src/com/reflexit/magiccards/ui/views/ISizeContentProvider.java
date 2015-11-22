package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.IContentProvider;

public interface ISizeContentProvider extends IContentProvider {
	int getSize(Object object);
}
