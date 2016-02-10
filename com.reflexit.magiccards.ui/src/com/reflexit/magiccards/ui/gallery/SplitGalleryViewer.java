package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.IColumnSortAction;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.SingleColumnTreeViewer;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class SplitGalleryViewer implements IMagicColumnViewer {
	private Composite control;
	protected LazyGalleryTreeViewer galleryviewer;
	private SingleColumnTreeViewer viewer;

	public SplitGalleryViewer(Composite parent, String preferencePageId) {
		createContents(parent);
	}

	@Override
	public Control getControl() {
		return control;
	}

	public Control createContents(Composite parent) {
		// Composite comp = new Composite(parent, SWT.NONE);
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		control = form;
		control.setLayout(new FillLayout());
		viewer = new SingleColumnTreeViewer(control);
		viewer.getTree().setHeaderVisible(false);
		galleryviewer = new LazyGalleryTreeViewer(control);
		galleryviewer.getControl().setFont(MagicUIActivator.getDefault().getFont());
		galleryviewer.setGroupsVisible(false);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				if (!ssel.isEmpty())
					galleryviewer.setInput(ssel.toList());
			}
		});
		form.setWeights(new int[] { 22, 78 });
		hookDragAndDrop();
		return control;
	}

	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId) {
			@Override
			protected GroupColumn createGroupColumn() {
				return new GroupColumn(true, true, false);
			}
		};
	}

	@Override
	public void dispose() {
		this.viewer.getLabelProvider().dispose();
		this.viewer.getControl().dispose();
		this.galleryviewer.getLabelProvider().dispose();
		this.galleryviewer.getControl().dispose();
		this.galleryviewer = null;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return galleryviewer;
	}

	@Override
	public ColumnViewer getViewer() {
		return viewer;
	}

	@Override
	public ColumnViewer getColumnViewer() {
		return viewer;
	}

	public StructuredViewer getStructuredViewer() {
		return galleryviewer;
	}

	@Override
	public void addDoubleClickListener(IDoubleClickListener doubleClickListener) {
		getStructuredViewer().addDoubleClickListener(doubleClickListener);
	}

	@Override
	public void hookDragAndDrop() {
		viewer.hookDragAndDrop(getStructuredViewer());
	}

	@Override
	public void setSortColumn(int index, int direction) {
		viewer.setSortColumn(index, direction);
		getStructuredViewer().setComparator(null);
	}

	@Override
	public void setInput(Object input) {
		if (viewer == null || viewer.getControl().isDisposed())
			return;
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		viewer.setSelection(new StructuredSelection());
		viewer.setInput(input);
		if (galleryviewer == null || this.galleryviewer.getControl().isDisposed())
			return;
		if (input instanceof IFilteredCardStore) {
			if (selection.isEmpty()) {
				IFilteredCardStore fstore = (IFilteredCardStore) input;
				ICardGroup group = fstore.getCardGroupRoot();
				selection = new StructuredSelection(group);
				viewer.setSelection(selection, true);
			} else {
				viewer.setSelection(selection, true);
			}
			galleryviewer.refresh(true);
		}
	}

	@Override
	public ColumnCollection getColumnsCollection() {
		return viewer.getColumnsCollection();
	}

	@Override
	public boolean hookContextMenu(MenuManager menuMgr) {
		Control gcontrol = galleryviewer.getControl();
		Menu menu = menuMgr.createContextMenu(gcontrol);
		gcontrol.setMenu(menu);
		return true;
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateColumns(String preferenceValue) {
		// ignore
	}

	@Override
	public void refresh() {
		viewer.refresh();
	}

	@Override
	public void setLinesVisible(boolean grid) {
		viewer.setLinesVisible(grid);
	}

	@Override
	public int getSortDirection() {
		return 0;
	}

	@Override
	public String getColumnLayoutProperty() {
		return null;
	}

	@Override
	public void hookContext(String id) {
		viewer.hookContext(id);
	}
}
