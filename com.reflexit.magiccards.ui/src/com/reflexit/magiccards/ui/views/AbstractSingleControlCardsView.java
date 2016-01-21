package com.reflexit.magiccards.ui.views;

import java.io.IOException;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;

public abstract class AbstractSingleControlCardsView extends AbstractCardsView {
	private AbstractMagicCardsListControl magicControl;

	public AbstractSingleControlCardsView() {
		magicControl = createViewControl();
	}

	protected abstract AbstractMagicCardsListControl createViewControl();

	protected IViewPage getMagicControl() {
		return magicControl;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getMagicControl().init(this);
	}

	@Override
	protected void hookContextMenu() {
		// magic control hooks the menu, we just need to register it and hook
		// our fill method
		MenuManager menuMgr = getMagicControl().getContextMenuManager();
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		registerContextMenu(menuMgr);
	}

	@Override
	protected void createMainControl(Composite parent) {
		getMagicControl().createContents(parent);
	}

	@Override
	protected void activate() {
		clearActionBars();
		getMagicControl().activate();
		contributeToActionBars();
		registerSelectionProvider();
	}

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return getMagicControl().getSelectionProvider();
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.remove(actionRefresh.getId());
		super.fillLocalPullDown(manager);
	}

	@Override
	public void dispose() {
		getMagicControl().dispose();
		super.dispose();
	}

	@Override
	public void setFocus() {
		getMagicControl().getControl().setFocus();
	}

	@Override
	public void reloadData() {
		getMagicControl().refresh();
	}

	/**
	 * @return
	 */
	public IPersistentPreferenceStore getLocalPreferenceStore() {
		if (getMagicControl() instanceof IMagicCardListControl)
			return ((IMagicCardListControl) getMagicControl()).getColumnsPreferenceStore();
		return null;
	}

	@Override
	public IPersistentPreferenceStore getFilterPreferenceStore() {
		if (getMagicControl() instanceof IMagicCardListControl)
			return ((IMagicCardListControl) getMagicControl()).getElementPreferenceStore();
		return null;
	}

	@Override
	public IFilteredCardStore getFilteredStore() {
		return ((IMagicCardListControl) getMagicControl()).getFilteredStore();
	}

	@Override
	public MagicCardFilter getFilter() {
		return getFilteredStore().getFilter();
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		if (!getViewSite().getPage().isPartVisible(this))
			return;
		getMagicControl().saveState(memento);
		try {
			getFilterPreferenceStore().save(); // XXX
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}
}
