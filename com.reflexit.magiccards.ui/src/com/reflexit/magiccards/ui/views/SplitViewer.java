package com.reflexit.magiccards.ui.views;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
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

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.ArrayCardStorage;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.SelectionProviderIntermediate;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.model.ExpandContentProvider;
import com.reflexit.magiccards.ui.views.model.ISizeContentProvider;

public class SplitViewer implements IMagicColumnViewer {
	private ExtendedTreeViewer treeviewer;
	private ExtendedTableViewer viewer;
	private SelectionProviderIntermediate selProvider;
	private int expansionLevel = 3;
	private IFilteredCardStore fstore;
	private Composite main;

	public SplitViewer(Composite parent, String id) {
		main = (Composite) createContents(parent, id);
	}

	@Override
	public Control getControl() {
		return main;
	}

	public Control createContents(Composite parent, String id) {
		// Composite comp = new Composite(parent, SWT.NONE);
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		Composite comp = form;
		treeviewer = new SingleColumnTreeViewer(comp) {
			@Override
			public IStructuredSelection translateSelection(IStructuredSelection selection, int level) {
				return super.translateSelection(selection, expansionLevel);
			}
		};
		viewer = new SimpleTableViewer(comp, doGetColumnCollection(id));
		viewer.getTable().setLayoutData(null);
		viewer.setSorter(null);
		viewer.setContentProvider(new ExpandContentProvider(true));
		// viewer.hookDragAndDrop();
		viewer.getControl().setLayoutData(null);
		treeviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				// System.err.println("selection changed " + ssel);
				List root = ssel.toList();
				if (!ssel.isEmpty()) {
					viewer.setSelection(new StructuredSelection());
					// ExpandContentProvider expandProvider = new
					// ExpandContentProvider();
					// expandProvider.inputChanged(viewer, null, root);
					// ArrayCardStorage storage = new
					// ArrayCardStorage<>(expandProvider.getElements(root),
					// fstore.getLocation());
					viewer.setInput(root);
				} else {
					int size = ((ISizeContentProvider) treeviewer.getContentProvider()).getSize(fstore);
					if (size == 0 && fstore != null) {
						viewer.setInput(new ArrayCardStorage<>(new Object[0], fstore.getLocation()));
					}
				}
			}
		});
		selProvider = new SelectionProviderIntermediate() {
			@Override
			public void setSelection(ISelection selection) {
				SplitViewer.this.setSelection(selection);
			}
		};
		selProvider.addDelegate(treeviewer);
		selProvider.addDelegate(viewer);
		form.setLayout(new FillLayout());
		form.setWeights(new int[] { 22, 78 });
		return form;
	}

	public void setSelection(ISelection selection) {
		if (treeviewer == null)
			return;
		if (viewer == null)
			return;
		if (selection.isEmpty()) {
			treeviewer.setSelection(selection, false);
			viewer.setSelection(selection, false);
		} else {
			viewer.setSelection(selection, true);
			if (viewer.getSelection().isEmpty()) {
				// it was not there, so re-select element in the tree
				treeviewer.setSelection(selection, true);
				viewer.setSelection(selection, true);
			}
		}
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
		treeviewer.getLabelProvider().dispose();
		treeviewer.getControl().dispose();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return selProvider;
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
		if (viewer == null || this.viewer.getControl().isDisposed())
			return;
		if (input instanceof IFilteredCardStore) {
			fstore = (IFilteredCardStore) input;
			IStructuredSelection selection = (IStructuredSelection) treeviewer.getSelection();
			treeviewer.setSelection(new StructuredSelection());
			treeviewer.setInput(input);
			expansionLevel = 3;
			if (fstore.getFilter().getGroupField() == MagicCardField.TYPE)
				expansionLevel = 5;
			treeviewer.expandToLevel(expansionLevel - 1);
			if (selection.isEmpty()) {
				ICardGroup group = fstore.getCardGroupRoot();
				selection = new StructuredSelection(group);
				treeviewer.setSelection(selection, true);
			} else {
				treeviewer.setSelection(selection, true);
			}
		}
	}

	@Override
	public boolean hookContextMenu(MenuManager menuMgr) {
		return viewer.hookContextMenu(menuMgr);
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		viewer.hookSortAction(sortAction);
	}

	@Override
	public void refresh() {
		treeviewer.refresh(true);
		viewer.refresh(true);
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
