package com.reflexit.magiccards.ui.views.nav;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.DataManager;

public class CardsNavigatiorManager implements IDisposable {
	private TreeViewer viewer;

	public CardsNavigatiorManager() {
	}

	public Control createContents(Composite parent, int flags) {
		this.viewer = new TreeViewer(parent, flags | SWT.FULL_SELECTION | SWT.VIRTUAL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new CardsNavigatorContentProvider());
		this.viewer.setLabelProvider(new CardsNavigatorLabelProvider());
		this.viewer.setUseHashlookup(true);
		this.viewer.setSorter(new ViewerSorter() {
		});
		this.viewer.setInput(DataManager.getModelRoot());
		return this.viewer.getControl();
	}

	public ColumnViewer getViewer() {
		return this.viewer;
	}

	public void dispose() {
		this.viewer = null;
	}
}
