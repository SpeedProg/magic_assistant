package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.views.SortOrderViewerComparator;
import com.reflexit.magiccards.ui.views.ViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;

public class GalleryViewerManager extends ViewerManager {
	private SortOrderViewerComparator vcomp = new SortOrderViewerComparator();
	protected LazyGalleryTreeViewer viewer;
	private FlatTreeContentProvider flatTreeContentProvider;

	public GalleryViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new LazyGalleryTreeViewer(parent);
		this.viewer.getControl().setFont(getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		flatTreeContentProvider = new FlatTreeContentProvider();
		flatTreeContentProvider.setLevel(5);
		this.viewer.setContentProvider(flatTreeContentProvider);
		getViewer().setLabelProvider(new MagicCardImageLabelProvider(viewer));
		this.viewer.setUseHashlookup(true);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		// createDefaultColumns();
		return this.viewer.getControl();
	}

	@Override
	public void dispose() {
		this.viewer.getLabelProvider().dispose();
		this.viewer.getControl().dispose();
		this.viewer = null;
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	@Override
	public void updateColumns(String value) {
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.updateColumnsFromPropery(value);
		columnsCollection.moveColumnOnTop(columnsCollection.getColumn(GroupColumn.COL_NAME));
	}

	@Override
	public String getColumnLayoutProperty() {
		ColumnCollection columnsCollection = getColumnsCollection();
		return columnsCollection.getColumnLayoutProperty();
	}

	@Override
	public void setLinesVisible(boolean grid) {
		// nothing
	}

	@Override
	public void setSortColumn(int index, int direction) {
		boolean sort = index >= 0;
		if (sort) {
			AbstractColumn man = getColumn(index);
			boolean oldAccending = vcomp.getComparator().getComparator(man.getSortField()).isAccending();
			boolean newAccending = !oldAccending;
			vcomp.setOrder(man.getSortField(), newAccending);
			this.viewer.setComparator(vcomp);
		} else {
			this.viewer.setComparator(null);
		}
	}

	@Override
	public int getSortDirection() {
		return SWT.UP;
	}

	protected IContentProvider getContentProvider() {
		return getViewer().getContentProvider();
	}

	@Override
	public void updateViewer(Object input) {
		if (viewer == null || this.viewer.getControl().isDisposed())
			return;
		updateTableHeader();
		updateGrid();
		this.viewer.setInput(input);
	}
}
