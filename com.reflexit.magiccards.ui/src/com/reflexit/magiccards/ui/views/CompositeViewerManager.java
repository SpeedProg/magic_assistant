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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * @author Alena
 *
 */
public class CompositeViewerManager extends ViewerManager {
	ViewerManager managers[];
	int activeIndex = 0;
	private StackLayout stackLayout;
	private Composite comp;
	private ISelectionProvider selectionProvider;

	/**
	 * @param handler
	 * @param store
	 * @param viewId
	 */
	public CompositeViewerManager(AbstractCardsView view) {
		super(view.getPreferenceStore(), view.getViewSite().getId());
		this.managers = new ViewerManager[2];
		this.managers[0] = new LazyTableViewerManager(view);
		this.managers[1] = new LazyTreeViewerManager(view);
		this.view = view;
		for (ViewerManager m : this.managers) {
			m.setFilter(this.filter);
		}
		this.selectionProvider = new ISelectionProvider() {
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
				for (ViewerManager m : CompositeViewerManager.this.managers) {
					m.getViewer().addSelectionChangedListener(listener);
				}
			}

			public ISelection getSelection() {
				return getViewer().getSelection();
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				for (ViewerManager m : CompositeViewerManager.this.managers) {
					m.getViewer().removeSelectionChangedListener(listener);
				}
			}

			public void setSelection(ISelection selection) {
				getViewer().setSelection(selection);
			}
		};
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.ViewerManager#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createContents(Composite parent) {
		this.comp = new Composite(parent, SWT.NONE);
		this.stackLayout = new StackLayout();
		this.comp.setLayout(this.stackLayout);
		for (ViewerManager m : this.managers) {
			Control control = m.createContents(this.comp);
		}
		setActivePage(this.activeIndex);
		this.comp.layout();
		return this.comp;
	}

	public void setActivePage(int i) {
		this.stackLayout.topControl = this.managers[i].getViewer().getControl();
		//this.view.getSite().setSelectionProvider(selectionProvider);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.ViewerManager#getViewer()
	 */
	@Override
	public ColumnViewer getViewer() {
		return this.managers[this.activeIndex].getViewer();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.ViewerManager#updateSortColumn(int)
	 */
	@Override
	public void updateSortColumn(int index) {
		for (ViewerManager m : this.managers) {
			m.updateSortColumn(index);
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.ViewerManager#updateColumns(java.lang.String)
	 */
	@Override
	public void updateColumns(String newValue) {
		for (ViewerManager m : this.managers) {
			m.updateColumns(newValue);
		}
	}

	/**
	 * @param indexCmc
	 */
	@Override
	public void updateGroupBy(ICardField fieldIndex) {
		ICardField oldIndex = this.filter.getGroupField();
		if (oldIndex == fieldIndex)
			return;
		this.filter.setGroupField(fieldIndex);
		if (oldIndex == null && fieldIndex != null) {
			// flip to tree
			this.activeIndex = 1;
		} else if (oldIndex != null && fieldIndex == null) {
			// flip to table
			this.activeIndex = 0;
		}
		if (this.stackLayout != null)
			setActivePage(this.activeIndex);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.ViewerManager#updateViewer()
	 */
	@Override
	public void updateViewer() {
		if (this.comp.isDisposed()) return;
		this.managers[this.activeIndex].updateViewer();
		this.comp.layout();
	}

	@Override
	public void addDoubleClickListener(IDoubleClickListener doubleClickListener) {
		for (ViewerManager m : this.managers) {
			m.addDoubleClickListener(doubleClickListener);
		}
	}

	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		for (ViewerManager m : this.managers) {
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
	public void setFilteredCardStore(IFilteredCardStore store) {
		for (ViewerManager m : this.managers) {
			m.setFilteredCardStore(store);
		}
		super.setFilteredCardStore(store);
	}
}
