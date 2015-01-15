package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;

public interface IMagicCardListControl extends IMagicControl {
	public abstract MagicCardFilter getFilter();

	public abstract IFilteredCardStore getFilteredStore();

	public abstract ISelection getSelection();

	public abstract PrefixedPreferenceStore getLocalPreferenceStore();

	public abstract PrefixedPreferenceStore getFilterPreferenceStore();

	public abstract void reloadData();

	public abstract void updateSingle(ICard source);

	public abstract void setNextSelection(ISelection structuredSelection);

	public abstract void setStatus(String string);

	public abstract IMenuManager getGroupMenu();
}