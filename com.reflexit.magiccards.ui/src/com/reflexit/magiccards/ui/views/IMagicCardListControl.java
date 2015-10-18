package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.ISelection;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public interface IMagicCardListControl extends IMagicControl {
	public abstract MagicCardFilter getFilter();

	public abstract IFilteredCardStore getFilteredStore();

	public abstract ISelection getSelection();

	public abstract IPersistentPreferenceStore getLocalPreferenceStore();

	public abstract IPersistentPreferenceStore getFilterPreferenceStore();

	@Override
	public abstract void reloadData();

	public abstract void setNextSelection(ISelection structuredSelection);

	@Override
	public abstract void setStatus(String string);

	public abstract IMenuManager getGroupMenu();
}