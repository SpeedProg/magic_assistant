/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.utils.SelectionProviderIntermediate;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

/**
 * @author Alena
 * 
 */
public class CompositeViewerManager extends ViewerManager {
	private ViewerManager managers[];
	private int activeIndex = 0;
	private StackLayout stackLayout;
	private Composite comp;
	private SelectionProviderIntermediate selectionProvider;

	public CompositeViewerManager(String id) {
		super(id);
		this.managers = new ViewerManager[2];
		this.managers[0] = new LazyTableViewerManager(id) {
			@Override
			protected ColumnCollection doGetColumnCollection(String prefPageId) {
				return CompositeViewerManager.this.doGetColumnCollection(prefPageId);
			}
		};
		this.managers[1] = new LazyTreeViewerManager(id) {
			@Override
			protected ColumnCollection doGetColumnCollection(String prefPageId) {
				return CompositeViewerManager.this.doGetColumnCollection(prefPageId);
			}
		};
		this.selectionProvider = new SelectionProviderIntermediate();
		this.selectionProvider.setSelectionProviderDelegate(getViewer());
	}

	public TreeViewerManager getTreeViewerManager() {
		return (TreeViewerManager) managers[1];
	}

	public TableViewerManager getTableViewerManager() {
		return (TableViewerManager) managers[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.reflexit.magiccards.ui.views.ViewerManager#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	public Control createContents(Composite parent) {
		this.comp = new Composite(parent, SWT.NONE);
		this.stackLayout = new StackLayout();
		this.comp.setLayout(this.stackLayout);
		for (IMagicColumnViewer m : this.managers) {
			m.createContents(this.comp);
		}
		hookDragAndDrop();
		setActivePage(this.activeIndex);
		this.comp.layout();
		return this.comp;
	}

	@Override
	public ColumnCollection getColumnsCollection() {
		return this.managers[this.activeIndex].getColumnsCollection();
	}

	@Override
	public final void hookDragAndDrop() {
		for (IMagicColumnViewer m : this.managers) {
			hookDragAndDrop(m);
		}
	}

	@Override
	public void hookContext(String id) {
		for (IMagicColumnViewer m : this.managers) {
			m.hookContext(id);
		}
	}

	protected void hookDragAndDrop(IMagicColumnViewer m) {
		m.hookDragAndDrop();
	}

	public void setActivePage(int i) {
		this.stackLayout.topControl = this.managers[i].getViewer().getControl();
		// this.view.getSite().setSelectionProvider(selectionProvider);
		this.selectionProvider.setSelectionProviderDelegate(managers[i].getViewer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.ViewerManager#getViewer()
	 */
	@Override
	public ColumnViewer getViewer() {
		return this.managers[this.activeIndex].getViewer();
	}

	@Override
	public void setSortColumn(int index, int direction) {
		for (IMagicColumnViewer m : this.managers) {
			m.setSortColumn(index, direction);
		}
	}

	@Override
	public void updateColumns(String newValue) {
		for (IMagicColumnViewer m : this.managers) {
			m.updateColumns(newValue);
		}
	}

	@Override
	public void flip(boolean hasGroups) {
		if (hasGroups) {
			// flip to tree
			this.activeIndex = 1;
		} else {
			// flip to table
			this.activeIndex = 0;
		}
		if (this.stackLayout != null)
			setActivePage(this.activeIndex);
	}

	@Override
	public void updateViewer(Object input) {
		if (this.comp.isDisposed())
			return;
		this.managers[this.activeIndex].updateViewer(input);
		this.comp.layout();
	}

	@Override
	public void refresh() {
		if (this.comp.isDisposed())
			return;
		this.managers[this.activeIndex].refresh();
		this.comp.layout();
	}

	@Override
	public void setLinesVisible(boolean grid) {
		for (IMagicColumnViewer m : this.managers) {
			m.setLinesVisible(grid);
		}
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		for (IMagicColumnViewer m : this.managers) {
			m.hookSortAction(sortAction);
		}
		super.hookSortAction(sortAction);
	}

	@Override
	public void hookDoubleClickListener(IDoubleClickListener doubleClickListener) {
		for (IMagicColumnViewer m : this.managers) {
			m.hookDoubleClickListener(doubleClickListener);
		}
	}

	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		for (IMagicColumnViewer m : this.managers) {
			m.hookContextMenu(menuMgr);
		}
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return this.selectionProvider;
	}

	@Override
	public Control getControl() {
		return comp;
	}

	@Override
	public int getSortDirection() {
		return this.managers[this.activeIndex].getSortDirection();
	}

	@Override
	public String getColumnLayoutProperty() {
		for (IMagicColumnViewer m : this.managers) {
			m.getColumnLayoutProperty();
		}
		return this.managers[this.activeIndex].getColumnLayoutProperty();
	}
}
