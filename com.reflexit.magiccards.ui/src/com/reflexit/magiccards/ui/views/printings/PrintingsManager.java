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

import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.ViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.LocationColumn;
import com.reflexit.magiccards.ui.views.columns.SetColumn;

public class PrintingsManager extends ViewerManager implements IDisposable {
	protected PrintingsManager(AbstractCardsView view) {
		super(view.getPreferenceStore(), view.getViewSite().getId());
		this.view = view;
	}

	private TreeViewer viewer;
	private PrintingsViewerComparator vcomp = new PrintingsViewerComparator();

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new PrintingsContentProvider());
		this.viewer.setUseHashlookup(true);
		this.viewer.setComparator(null);
		createDefaultColumns();
		addDragAndDrop();
		return this.viewer.getControl();
	}

	@Override
	public void addDragAndDrop() {
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
	protected void createColumns() {
		this.columns.add(new SetColumn(true));
		this.columns.add(new CountColumn());
		this.columns.add(new LocationColumn());
	}

	protected void createDefaultColumns() {
		createColumnLabelProviders();
		for (int i = 0; i < getColumnsNumber(); i++) {
			AbstractColumn man = this.columns.get(i);
			TreeViewerColumn colv = new TreeViewerColumn(this.viewer, i);
			TreeColumn col = colv.getColumn();
			col.setText(man.getColumnName());
			col.setWidth(man.getColumnWidth());
			col.setToolTipText(man.getColumnTooltip());
			final int coln = i;
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sort(coln);
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
	protected void sort(int index) {
		updateSortColumn(index);
		getViewer().refresh();
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
		viewer.refresh(true);
	}
}
