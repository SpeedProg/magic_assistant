package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.SelectionProviderIntermediate;

/**
 * View page which has a group as main control
 * 
 */
public abstract class AbstractGroupPageCardsViewPage extends AbstractViewPage {
	private Composite main;
	private ViewPageGroup pageGroup;
	private SelectionProviderIntermediate selectionProviderBridge = new SelectionProviderIntermediate();

	public AbstractGroupPageCardsViewPage() {
		pageGroup = createPageGroup();
		createPages();
	}

	protected ViewPageGroup createPageGroup() {
		return new StackPageGroup(this::preActivate, this::postActivate);
	}

	protected abstract void createPages();

	public ViewPageGroup getPageGroup() {
		return pageGroup;
	}

	@Override
	public void init(IViewPart site) {
		super.init(site);
		pageGroup.init(site);
	}

	@Override
	public void dispose() {
		pageGroup.dispose();
		super.dispose();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return selectionProviderBridge;
	}

	@Override
	protected void createPageContents(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(GridLayoutFactory.fillDefaults().create());
		// Pages
		pageGroup.createContent(main);
	}

	@Override
	public void activate() {
		pageGroup.activate();
		super.activate();
	}

	protected synchronized IViewPage getActivePage() {
		return pageGroup.getActivePage();
	}

	public IFilteredCardStore getFilteredStore() {
		return ((IMagicCardListControl) getMainPage()).getFilteredStore();
	}

	protected void preActivate(IViewPage activePage) {
		// noting for bow
	}

	protected void postActivate(IViewPage activePage) {
		// contribute this group's extra actions
		contributeToActionBars();
		registerSelectionProvider();
	}

	protected void registerSelectionProvider() {
		selectionProviderBridge.setSelectionProviderDelegate(pageGroup.getActivePage().getSelectionProvider());
	}

	@Override
	public void refresh() {
		if (getActivePage() != null)
			getActivePage().refresh();
	}

	public IPersistentPreferenceStore getLocalPreferenceStore() {
		IViewPage magicControl = getActivePage();
		if (magicControl instanceof IMagicCardListControl)
			return ((IMagicCardListControl) magicControl).getColumnsPreferenceStore();
		magicControl = getMainPage();
		if (magicControl instanceof IMagicCardListControl)
			return ((IMagicCardListControl) magicControl).getColumnsPreferenceStore();
		return null;
	}

	protected IViewPage getMainPage() {
		return pageGroup.getPage(0);
	}

	public IPersistentPreferenceStore getFilterPreferenceStore() {
		if (getActivePage() instanceof IMagicCardListControl)
			return ((IMagicCardListControl) getActivePage()).getElementPreferenceStore();
		return null;
	}
}