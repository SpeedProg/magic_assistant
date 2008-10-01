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

/**
 * @author Alena
 *
 */
public class CompositeViewerManager extends ViewerManager {
	ViewerManager managers[];
	AbstractCardsView view;
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
		super(view.doGetFilteredStore(), view.getPreferenceStore(), view.getViewSite().getId());
		this.managers = new ViewerManager[2];
		this.managers[0] = new LazyTableViewerManager(getFilteredStore(), view);
		this.managers[1] = new LazyTreeViewerManager(getFilteredStore(), view);
		this.view = view;
		for (int i = 0; i < this.managers.length; i++) {
			ViewerManager m = this.managers[i];
			m.setFilter(this.filter);
		}
		this.selectionProvider = new ISelectionProvider() {
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
				for (int i = 0; i < CompositeViewerManager.this.managers.length; i++) {
					ViewerManager m = CompositeViewerManager.this.managers[i];
					m.getViewer().addSelectionChangedListener(listener);
				}
			}

			public ISelection getSelection() {
				return getViewer().getSelection();
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				for (int i = 0; i < CompositeViewerManager.this.managers.length; i++) {
					ViewerManager m = CompositeViewerManager.this.managers[i];
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
		for (int i = 0; i < this.managers.length; i++) {
			ViewerManager m = this.managers[i];
			Control control = m.createContents(this.comp);
		}
		setActivePage(this.activeIndex);
		this.comp.layout();
		return this.comp;
	}

	public void setActivePage(int i) {
		this.stackLayout.topControl = this.managers[i].getViewer().getControl();
		this.view.getSite().setSelectionProvider(this.managers[i].getViewer());
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
	protected void updateSortColumn(int index) {
		for (int i = 0; i < this.managers.length; i++) {
			ViewerManager m = this.managers[i];
			m.updateSortColumn(index);
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.ViewerManager#updateColumns(java.lang.String)
	 */
	@Override
	public void updateColumns(String newValue) {
		for (int i = 0; i < this.managers.length; i++) {
			ViewerManager m = this.managers[i];
			m.updateColumns(newValue);
		}
	}

	/**
	 * @param indexCmc
	 */
	@Override
	public void updateGroupBy(int fieldIndex) {
		int oldIndex = this.filter.getGroupIndex();
		if (oldIndex == fieldIndex)
			return;
		this.filter.setGroupIndex(fieldIndex);
		if (oldIndex < 0 && fieldIndex >= 0) {
			// flip to tree
			this.activeIndex = 1;
		} else if (oldIndex >= 0 && fieldIndex < 0) {
			// flip to table
			this.activeIndex = 0;
		}
		setActivePage(this.activeIndex);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.ViewerManager#updateViewer()
	 */
	@Override
	protected void updateViewer() {
		this.managers[this.activeIndex].updateViewer();
		this.comp.layout();
	}

	@Override
	public void addDoubleClickListener(IDoubleClickListener doubleClickListener) {
		for (int i = 0; i < this.managers.length; i++) {
			ViewerManager m = this.managers[i];
			m.addDoubleClickListener(doubleClickListener);
		}
	}

	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		for (int i = 0; i < this.managers.length; i++) {
			ViewerManager m = this.managers[i];
			m.hookContextMenu(menuMgr);
		}
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return this.selectionProvider;
	}
}
