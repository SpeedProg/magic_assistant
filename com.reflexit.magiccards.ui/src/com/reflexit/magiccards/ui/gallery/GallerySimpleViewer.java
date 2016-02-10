package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.views.IColumnSortAction;
import com.reflexit.magiccards.ui.views.IMagicViewer;
import com.reflexit.magiccards.ui.views.ViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.model.SortOrderViewerComparator;

public class GallerySimpleViewer extends LazyGalleryTreeViewer implements IMagicViewer {
	private ViewerManager manager;

	public GallerySimpleViewer(Composite parent, String id) {
		super(parent);
		manager = new ViewerManager(this, new MagicColumnCollection(id));
		manager.setCollumns(new MagicColumnCollection(id));
		createContents();
	}

	public Control createContents() {
		getControl().setFont(ViewerManager.getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		// flatTreeContentProvider = new FlatTreeContentProvider();
		// flatTreeContentProvider.setLevel(5);
		// setContentProvider(flatTreeContentProvider);
		updatePresentation();
		// viewer.setSorter(new NameSorter());
		// createDefaultColumns();
		// hookDoubleClickListener(new IDoubleClickListener() {
		// @Override
		// public void doubleClick(DoubleClickEvent event) {
		// flatTreeContentProvider.setDetails(event.getSelection());
		// viewer.refresh();
		// }
		// });
		// getViewer().setGroupsVisible(false);
		return getControl();
	}

	@Override
	public void dispose() {
		getLabelProvider().dispose();
		getControl().dispose();
	}

	@Override
	public LazyGalleryTreeViewer getViewer() {
		return this;
	}

	@Override
	public void setLinesVisible(boolean grid) {
		// nothing
	}

	public void setSortColumn(int index, int direction) {
		boolean sort = index >= 0;
		if (sort) {
			AbstractColumn man = manager.getColumn(index);
			SortOrderViewerComparator vcomp = manager.getViewerComparator();
			if (direction == 0) {
				boolean oldAccending = vcomp.getComparator().getComparator(man.getSortField()).isAccending();
				boolean newAccending = !oldAccending;
				vcomp.setOrder(man.getSortField(), newAccending);
			} else {
				vcomp.setOrder(man.getSortField(), direction == -1);
			}
			setComparator(vcomp);
		} else {
			setComparator(null);
		}
	}

	public int getSortDirection() {
		return SWT.UP;
	}

	private void updatePresentation() {
		// TODO Auto-generated method stub
	}

	public ColumnViewer getColumnViewer() {
		return getViewer();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return getViewer();
	}

	@Override
	public void refresh() {
		super.refresh();
		updatePresentation();
	}

	@Override
	public boolean hookContextMenu(MenuManager menuMgr) {
		return manager.hookContextMenu(menuMgr);
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		manager.hookSortAction(sortAction);
	}

	@Override
	public void hookDragAndDrop() {
		ViewerManager.hookDragAndDrop(getViewer());
	}

	@Override
	public void hookContext(String id) {
		manager.hookContext(id);
	}
}
