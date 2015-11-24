package com.reflexit.magiccards.ui.views;

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

import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class SplitViewer implements IMagicColumnViewer {
	private ExtendedTreeViewer treeviewer;
	private ExtendedTableViewer viewer;

	public SplitViewer(Composite parent, String id) {
		createContents(parent, id);
	}

	@Override
	public Control getControl() {
		return treeviewer.getControl().getParent();
	}

	public Control createContents(Composite parent, String id) {
		// Composite comp = new Composite(parent, SWT.NONE);
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		Composite comp = form;
		treeviewer = new SingleColumnTreeViewer(comp);
		viewer = new SimpleTableViewer(comp, doGetColumnCollection(id));
		viewer.getTable().setLayoutData(null);
		viewer.setSorter(null);
		viewer.setContentProvider(new ExpandContentProvider());
		viewer.getControl().setLayoutData(null);
		treeviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				if (!ssel.isEmpty()) {
					// viewer.setInput(ssel.toList());
					viewer.setInput(ssel.toList());
				}
			}
		});
		form.setLayout(new FillLayout());
		form.setWeights(new int[] { 22, 78 });
		return comp;
	}

	public boolean supportsGroupping(boolean groupped) {
		return true;
	};

	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId) {
			@Override
			protected GroupColumn createGroupColumn() {
				return new GroupColumn(false, true, false);
			}
		};
	}

	@Override
	public void dispose() {
		viewer.getControl().dispose();
		viewer.getLabelProvider().dispose();
		this.treeviewer.getLabelProvider().dispose();
		this.treeviewer.getControl().dispose();
		this.treeviewer = null;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return viewer;
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
		return treeviewer;
	}

	@Override
	public void addDoubleClickListener(IDoubleClickListener doubleClickListener) {
		getStructuredViewer().addDoubleClickListener(doubleClickListener);
	}

	protected void createContentMenu() {
		Control control = getStructuredViewer().getControl();
		control.setMenu(viewer.getMenuManager().createContextMenu(control));
	}

	@Override
	public void hookDragAndDrop() {
		viewer.hookDragAndDrop();
		treeviewer.hookDragAndDrop();
	}

	@Override
	public void setInput(Object input) {
		if (treeviewer == null || this.treeviewer.getControl().isDisposed())
			return;
		IStructuredSelection selection = (IStructuredSelection) treeviewer.getSelection();
		treeviewer.setSelection(new StructuredSelection());
		treeviewer.setInput(input);
		if (viewer == null || this.viewer.getControl().isDisposed())
			return;
		if (input instanceof IFilteredCardStore) {
			if (selection.isEmpty()) {
				IFilteredCardStore fstore = (IFilteredCardStore) input;
				ICardGroup group = fstore.getCardGroupRoot();
				selection = new StructuredSelection(group);
				treeviewer.setSelection(selection, true);
			} else {
				treeviewer.setSelection(selection, true);
			}
			viewer.refresh(true);
		}
	}

	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		viewer.hookContextMenu(menuMgr);
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		viewer.hookSortAction(sortAction);
	}

	@Override
	public void refresh() {
		treeviewer.refresh(true);
	}

	@Override
	public void setLinesVisible(boolean grid) {
		viewer.setLinesVisible(grid);
		treeviewer.setLinesVisible(grid);
	}

	@Override
	public void hookContext(String id) {
		viewer.hookContext(id);
	}

	@Override
	public ColumnCollection getColumnsCollection() {
		return viewer.getColumnsCollection();
	}

	@Override
	public void updateColumns(String preferenceValue) {
		viewer.updateColumns(preferenceValue);
	}

	@Override
	public void setSortColumn(int index, int direction) {
		viewer.setSortColumn(index, direction);
	}

	@Override
	public int getSortDirection() {
		return viewer.getSortDirection();
	}

	@Override
	public String getColumnLayoutProperty() {
		return viewer.getColumnLayoutProperty();
	}
}
