package com.reflexit.magiccards.ui.views;

import java.util.ArrayList;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.SelectionProviderIntermediate;

public abstract class AbstractGroupPageCardsView extends AbstractCardsView {
	private Composite main;
	private ViewPageGroup pageGroup;
	private SelectionProviderIntermediate selectionProviderBridge = new SelectionProviderIntermediate();
	private MenuManager menuMgr;

	/**
	 * The constructor.
	 */
	public AbstractGroupPageCardsView() {
		pageGroup = createPageGroup();
		createPages();
		pageGroup.init(this);
	}

	protected ViewPageGroup createPageGroup() {
		return new FolderPageGroup() {
			@Override
			public void activate() {
				IViewPage activePage = pageGroup.getActivePage();
				preActivate(activePage);
				super.activate();
				postActivate(activePage);
			}
		};
	}

	protected abstract void createPages();

	public ViewPageGroup getPageGroup() {
		return pageGroup;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
	}

	@Override
	public void dispose() {
		pageGroup.dispose();
		super.dispose();
	}

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return selectionProviderBridge;
	}

	@Override
	protected void createMainControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(GridLayoutFactory.fillDefaults().create());
		// Pages
		pageGroup.createContent(main);
	}

	@Override
	protected void activate() {
		pageGroup.setActivePageIndex(0);
		pageGroup.activate();
	}

	protected void close() {
		try {
			getViewSite().getPage().hideView(AbstractGroupPageCardsView.this);
		} catch (Exception e) {
			// ignore
		}
	}

	protected synchronized IMagicControl getMagicControl(IViewPage page) {
		// System.err.println(deckPage);
		if (page instanceof IMagicControl) {
			return (IMagicControl) page;
		}
		return null;
	}

	protected synchronized IMagicControl getMagicControl() {
		IViewPage page = pageGroup.getActivePage();
		return getMagicControl(page);
	}

	@Override
	public IFilteredCardStore getFilteredStore() {
		return ((IMagicCardListControl) getMainMagicControl()).getFilteredStore();
	}

	@Override
	protected void hookContextMenu() {
		// register view menu
		registerContextMenu(getContextMenuManager());
	}

	@Override
	protected boolean hookContextMenu(MenuManager menuMgr) {
		// do nothing active page hooks it
		return true;
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
		IMagicControl active = getMagicControl();
		if (active != null)
			active.setGlobalHandlers(bars);
	}

	public IAction getGroupAction() {
		return ((AbstractMagicCardsListControl) getMagicControl()).getGroupAction();
	}

	public void setSelection(IStructuredSelection structuredSelection) {
		ArrayList<Object> l = new ArrayList<Object>();
		for (Object o : structuredSelection.toList()) {
			if (o instanceof MagicCard) {
				l.addAll(((MagicCard) o).getRealCards().getChildrenList());
				continue;
			} else if (o instanceof IMagicCard) {
				l.add(o);
			}
		}
		getSelectionProvider().setSelection(new StructuredSelection(l));
	}

	protected void preActivate(IViewPage activePage) {
		// clean toolbar
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.removeAll();
		toolBarManager.update(true);
		// clean local view menu
		IMenuManager viewMenuManager = bars.getMenuManager();
		viewMenuManager.removeAll();
		viewMenuManager.updateAll(true);
		bars.updateActionBars();
		// reset context menu
		menuMgr = createContentMenuManager();
		pageGroup.getActivePage().setContextMenuManager(menuMgr);
		// set fstore
		// activePage.setFilteredStore(getFilteredStore());
	}

	protected void postActivate(IViewPage activePage) {
		// contribute this view extra actions
		contributeToActionBars();
		selectionProviderBridge.setSelectionProviderDelegate(activePage.getSelectionProvider());
		getSite().setSelectionProvider(selectionProviderBridge);
	}

	private MenuManager getContextMenuManager() {
		return menuMgr;
	}

	@Override
	public void reloadData() {
		if (getMagicControl() != null)
			getMagicControl().reloadData();
	}

	public IPersistentPreferenceStore getLocalPreferenceStore() {
		IMagicControl magicControl = getMagicControl();
		if (magicControl instanceof IMagicCardListControl)
			return ((IMagicCardListControl) magicControl).getColumnsPreferenceStore();
		magicControl = getMainMagicControl();
		if (magicControl instanceof IMagicCardListControl)
			return ((IMagicCardListControl) magicControl).getColumnsPreferenceStore();
		return null;
	}

	protected IMagicControl getMainMagicControl() {
		return getMagicControl(pageGroup.getPage(0));
	}

	@Override
	public IPersistentPreferenceStore getFilterPreferenceStore() {
		if (getMagicControl() instanceof IMagicCardListControl)
			return ((IMagicCardListControl) getMagicControl()).getElementPreferenceStore();
		return null;
	}
}