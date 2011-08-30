package com.reflexit.magiccards.ui.views.printings;

import java.util.Collection;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.ViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.LocationColumn;
import com.reflexit.magiccards.ui.views.columns.OwnershipColumn;
import com.reflexit.magiccards.ui.views.columns.SetColumn;

public class PrintingsManager extends ViewerManager implements IDisposable {
	private TreeViewer viewer;
	private PrintingsViewerComparator vcomp = new PrintingsViewerComparator();
	private boolean dbMode = true;

	protected PrintingsManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new PrintingsContentProvider());
		this.viewer.setUseHashlookup(true);
		this.viewer.setComparator(null);
		createDefaultColumns();
		hookDragAndDrop();
		updateDbMode(true);
		return this.viewer.getControl();
	}

	@Override
	public void hookDragAndDrop() {
		this.getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer()));
	}

	public void setInput(Collection<Object> input) {
		this.viewer.setInput(input);
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	@Override
	public void dispose() {
		this.viewer = null;
	}

	@Override
	protected ColumnCollection doGetColumnCollection(String viewId) {
		return new ColumnCollection() {
			@Override
			protected void createColumns() {
				columns.add(new GroupColumn());
				columns.add(new SetColumn(true));
				columns.add(new CountColumn());
				columns.add(new OwnershipColumn());
				columns.add(new LocationColumn());
			}
		};
	}

	protected void createDefaultColumns() {
		getColumnsCollection().createColumnLabelProviders();
		for (int i = 0; i < getColumnsNumber(); i++) {
			AbstractColumn man = getColumn(i);
			TreeViewerColumn colv = new TreeViewerColumn(this.viewer, i);
			TreeColumn col = colv.getColumn();
			col.setText(man.getColumnName());
			col.setWidth(man.getColumnWidth());
			col.setToolTipText(man.getColumnTooltip());
			final int coln = i;
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sortColumn(coln);
				}
			});
			col.setMoveable(false);
			colv.setLabelProvider(man);
			if (man instanceof Listener) {
				this.viewer.getTree().addListener(SWT.PaintItem, (Listener) man);
			}
			colv.setEditingSupport(man.getEditingSupport(this.viewer));
		}
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		this.viewer.getTree().setHeaderVisible(true);
	}

	@Override
	public void updateSortColumn(int index) {
		boolean sort = index >= 0;
		TreeColumn column = sort ? this.viewer.getTree().getColumn(index) : null;
		this.viewer.getTree().setSortColumn(column);
		if (sort) {
			int sortDirection = this.viewer.getTree().getSortDirection();
			if (sortDirection != SWT.DOWN)
				sortDirection = SWT.DOWN;
			else
				sortDirection = SWT.UP;
			this.viewer.getTree().setSortDirection(sortDirection);
			AbstractColumn man = (AbstractColumn) this.viewer.getLabelProvider(index);
			vcomp.setOrder(man.getSortField(), sortDirection == SWT.UP);
			this.viewer.setComparator(vcomp);
		} else {
			this.viewer.setComparator(null);
		}
	}

	@Override
	public void updateViewer() {
		if (this.viewer.getControl().isDisposed())
			return;
		updateTableHeader();
		updateGrid();
		IFilteredCardStore filteredStore = getFilteredStore();
		this.viewer.setInput(this.getFilteredStore());
	}

	@Override
	protected void updateTableHeader() {
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		hideColumn(0, getFilter().getGroupField() == null, acolumns);
		hideColumn(2, dbMode, acolumns);
		hideColumn(3, dbMode, acolumns);
		hideColumn(4, dbMode, acolumns);
	}

	private void hideColumn(int i, boolean hide, TreeColumn[] acolumns) {
		TreeColumn column = acolumns[i];
		if (hide)
			column.setWidth(0);
		else if (column.getWidth() <= 0) {
			int def = getColumn(i).getColumnWidth();
			column.setWidth(def);
		}
	}

	protected void updateGrid() {
		boolean grid = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_GRID);
		this.viewer.getTree().setLinesVisible(grid);
	}

	@Override
	public void updateGroupBy(ICardField field) {
		super.updateGroupBy(field);
	}

	public void updateDbMode(boolean checked) {
		dbMode = checked;
		if (dbMode)
			updateGroupBy(null);
	}

	public boolean isDbMode() {
		return dbMode;
	}
}
