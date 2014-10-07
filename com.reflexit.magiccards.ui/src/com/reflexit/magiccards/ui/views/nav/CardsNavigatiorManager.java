package com.reflexit.magiccards.ui.views.nav;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.ui.MagicUIActivator;

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
		this.viewer.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				if (element instanceof CardOrganizer)
					return 0;
				return 1;
			}
		});
		this.viewer.setAutoExpandLevel(3);
		this.viewer.setInput(DataManager.getInstance().getModelRoot());
		this.refresh();
		return this.viewer.getControl();
	}

	public ColumnViewer getViewer() {
		return this.viewer;
	}

	@Override
	public void dispose() {
		this.viewer = null;
	}

	public void refresh() {
		getViewer().getControl().setFont(MagicUIActivator.getDefault().getFont());
		getViewer().getControl().setForeground(MagicUIActivator.getDefault().getTextColor());
		this.viewer.refresh(true);
	}
}
