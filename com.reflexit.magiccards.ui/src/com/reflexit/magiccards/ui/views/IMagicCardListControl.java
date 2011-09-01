package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;

public interface IMagicCardListControl extends IMagicControl {
	public abstract MagicCardFilter getFilter();

	public abstract IFilteredCardStore getFilteredStore();

	public abstract ISelection getSelection();

	public abstract PrefixedPreferenceStore getLocalPreferenceStore();

	public abstract void reloadData();

	public abstract void setFilteredCardStore(IFilteredCardStore<ICard> fstore);

	public abstract void updateSingle(ICard source);

	public abstract void setNextSelection(StructuredSelection structuredSelection);

	public abstract void setStatus(String string);

	public abstract IMenuManager getGroupMenu();

	public abstract void setFilter(MagicCardFilter filter);
}