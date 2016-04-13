package com.reflexit.magiccards.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;

import com.reflexit.magiccards.ui.MagicUIActivator;

public class ViewPageGroup {
	private ArrayList<ViewPageContribution> pages = new ArrayList<>();
	private int activePageIndex = -1;
	private IViewPart view;
	private Consumer<IViewPage> beforeActivate;
	private Consumer<IViewPage> afterActivate;

	public ViewPageGroup(Consumer<IViewPage> beforeActivate, Consumer<IViewPage> afterActivate) {
		this.beforeActivate = beforeActivate;
		this.afterActivate = afterActivate;
	}

	public IViewPart getViewPart() {
		return view;
	}

	public void createContent(Composite parent) {
		for (ViewPageContribution page : pages) {
			safeRun(() -> {
				if (page.isInstantiated()) {
					Control control = page.getViewPage().getControl();
					if (control != null)
						control.dispose();
				}
			});
		}
		for (ViewPageContribution page : pages) {
			safeRun(() -> createPageContent(page, parent));
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

	public int getPageIndex(Control control) {
		int i = 0;
		for (ViewPageContribution page : pages) {
			if (page.isInstantiated() && page.getViewPage().getControl() == control)
				return i;
			i++;
		}
		return -1;
	}

	protected void createPageContent(ViewPageContribution vc, Composite parent) {
		Control con = vc.getViewPage().createContents(parent);
		con.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
	}

	public int size() {
		return pages.size();
	}

	public void init(IViewPart view) {
		this.view = view;
		for (ViewPageContribution page : pages) {
			safeRun(() -> page.getViewPage().init(view));
		}
	}

	public boolean activate(int page) {
		if (activePageIndex == page && pages.get(activePageIndex).isInstantiated())
			return false;
		deactivate();
		setActivePageIndex(page);
		activate();
		return true;
	}

	public void activate() {
		if (view == null)
			throw new NullPointerException();
		if (activePageIndex < 0)
			activePageIndex = 0;
		IViewPage activePage = getActivePage();
		if (beforeActivate != null)
			beforeActivate.accept(activePage);
		ViewPageContribution vc = pages.get(activePageIndex);
		vc.getViewPage().activate();
		if (afterActivate != null)
			afterActivate.accept(activePage);
	}

	public void setActivePageIndex(int page) {
		activePageIndex = page;
	}

	public void deactivate() {
		if (activePageIndex < 0 || activePageIndex >= size()) {
			// no prev page
		} else {
			ViewPageContribution vcOld = pages.get(activePageIndex);
			if (vcOld.isInstantiated()) {
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
		// getActivePage().activate();// XXX
	}

	public IViewPage getPage(int i) {
		return pages.get(i).getViewPage();
	}

	public List<ViewPageContribution> getPages() {
		return pages;
	}

	public void activate(String text) {
		int i = getPageIndex(text);
		if (i >= 0)
			activate(i);
	}

	public int getPageIndex(String text) {
		int i = 0;
		for (ViewPageContribution page : pages) {
			if (page.getName().equals(text))
				return i;
			i++;
		}
		return -1;
	}
}
