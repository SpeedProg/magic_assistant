package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.ISelection;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public interface IMagicCardListControl extends IViewPage {
	public abstract MagicCardFilter getFilter();

	public abstract IFilteredCardStore getFilteredStore();

	public abstract ISelection getSelection();

	public abstract IPersistentPreferenceStore getColumnsPreferenceStore();

	public abstract IPersistentPreferenceStore getElementPreferenceStore();

	@Override
	public abstract void refresh();

	public abstract void setNextSelection(ISelection structuredSelection);

	public abstract void setStatus(String string);
}