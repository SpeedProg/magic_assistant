package com.reflexit.magiccards.ui.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;

import com.reflexit.magiccards.ui.MagicUIActivator;

public class ViewPageGroup {
	private ArrayList<ViewPageContribution> pages = new ArrayList<>();
	private int activePageIndex = -1;
	private IViewPart view;
	private Composite parent;

	public ViewPageGroup() {
	}

	public IViewPart getViewPart() {
		return view;
	}

	public void createContent(Composite parent) {
		this.parent = parent;
		for (ViewPageContribution page : pages) {
			safeRun(() -> createPagePlaceholder(page, parent));
		}
	}

	public void add(ViewPageContribution page) {
		pages.add(page);
	};

	public int getPageIndex(IViewPage vpage) {
		int i = 0;
		for (ViewPageContribution page : pages) {
			if (page.isInstantiated() && page.getViewPage() == vpage)
				return i;
			i++;
		}
		return -1;
	}

	protected void createPagePlaceholder(ViewPageContribution page, Composite parent) {
		// this method may be overriden to create placeholder without
		// instantiating a page,
		// i.e. ctab with page name
	}

	public int size() {
		return pages.size();
	}

	public void init(IViewPart view) {
		this.view = view;
	}

	public boolean activate(int page) {
		if (activePageIndex == page)
			return false;
		deactivate();
		setActivePageIndex(page);
		activate();
		return true;
	}

	public void activate() {
		if (view == null)
			throw new NullPointerException();
		ViewPageContribution vc = pages.get(activePageIndex);
		if (!vc.isInitialized()) {
			vc.init(view);
			createPageContent(vc.getViewPage());
		}
		vc.getViewPage().activate();
	}

	public void setActivePageIndex(int page) {
		activePageIndex = page;
	}

	public void deactivate() {
		if (activePageIndex < 0 || activePageIndex >= size()) {
			// no prev page
		} else {
			ViewPageContribution vcOld = pages.get(activePageIndex);
			if (vcOld.isInitialized()) {
				safeRun(() -> vcOld.getViewPage().deactivate());
			}
		}
	}

	private void safeRun(Runnable run) {
		try {
			run.run();
		} catch (Throwable e) {
			MagicUIActivator.log(e);
		}
	}

	protected void createPageContent(IViewPage viewPage) {
		viewPage.createContents(parent);
	}

	public int getActivePageIndex() {
		return activePageIndex;
	}

	public IViewPage getActivePage() {
		return pages.get(activePageIndex).getViewPage();
	}

	public void loadExtensions(String thisId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(MagicUIActivator.PLUGIN_ID + ".deckPage");
		IConfigurationElement points[] = extensionPoint.getConfigurationElements();
		loadExtensions(points, thisId);
	}

	public void loadExtensions(IConfigurationElement points[], String thisId) {
		for (IConfigurationElement el : points) {
			String targetId = el.getAttribute("targetId");
			if (targetId == null && thisId == null || thisId != null && thisId.equals(targetId)) {
				add(ViewPageContribution.parseElement(el));
			}
		}
	}

	public void dispose() {
		for (ViewPageContribution page : pages) {
			if (page.isInstantiated())
				page.getViewPage().dispose();
		}
	}

	public void activate(IViewPage activePage) {
		int i = getPageIndex(activePage);
		if (i >= 0)
			activate(i);
	}

	public void refresh() {
		//getActivePage().activate();// XXX
	}

	public IViewPage getPage(int i) {
		return pages.get(i).getViewPage();
	}
}
