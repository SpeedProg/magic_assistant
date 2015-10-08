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

import java.util.Arrays;

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

	public int findManager(IMagicColumnViewer man) {
		for (int i = 0; i < managers.length; i++) {
			ViewerManager m = managers[i];
			if (m == man)
				return i;
		}
		return -1;
	}

	public int addManager(ViewerManager man) {
		int i = findManager(man);
		if (i >= 0)
			return i;
		i = managers.length;
		Arrays.copyOf(managers, i + 1);
		managers[i] = man;
		if (stackLayout != null) {
			man.createContents(comp);
			man.hookDragAndDrop();
		}
		return i;
	}

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
		return managers[activeIndex].getColumnsCollection();
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
		activeIndex = i;
		if (stackLayout != null)
			stackLayout.topControl = this.managers[i].getViewer().getControl();
		// this.view.getSite().setSelectionProvider(selectionProvider);
		this.selectionProvider.setSelectionProviderDelegate(managers[i].getViewer());
	}

	@Override
	public ColumnViewer getViewer() {
		return managers[activeIndex].getViewer();
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
	public void setGrouppingEnabled(boolean hasGroups) {
		if (managers[activeIndex].supportsGroupping(hasGroups))
			return;
		for (int i = 0; i < managers.length; i++) {
			if (managers[i].supportsGroupping(hasGroups)) {
				activeIndex = i;
				break;
			}
		}
		setActivePage(this.activeIndex);
	}

	@Override
	public void updateViewer(Object input) {
		if (this.comp.isDisposed())
			return;
		for (int i = 0; i < managers.length; i++) {
			if (i != activeIndex)
				managers[i].updateViewer(null);
			else
				managers[activeIndex].updateViewer(input);
		}
		this.comp.layout();
	}

	@Override
	public void refresh() {
		if (this.comp.isDisposed())
			return;
		managers[activeIndex].refresh();
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
		return managers[activeIndex].getSortDirection();
	}

	@Override
	public String getColumnLayoutProperty() {
		for (IMagicColumnViewer m : this.managers) {
			m.getColumnLayoutProperty();
		}
		return managers[activeIndex].getColumnLayoutProperty();
	}
}
