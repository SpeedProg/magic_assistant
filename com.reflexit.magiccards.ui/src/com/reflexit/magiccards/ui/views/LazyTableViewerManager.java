package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.services.IDisposable;

import java.util.HashMap;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardStore;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.MagicCardDragListener;
import com.reflexit.magiccards.ui.utils.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.utils.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.columns.ColumnManager;

public class LazyTableViewerManager extends ViewerManager implements IDisposable {
	private TableViewer viewer;
	private AbstractCardsView view;

	public LazyTableViewerManager(AbstractCardsView view) {
		super(view.doGetFilteredStore(), view.getPreferenceStore(), view.getViewSite().getId());
		this.view = view;
	}

	public LazyTableViewerManager(IFilteredCardStore fs, AbstractCardsView view) {
		super(fs, view.getPreferenceStore(), view.getViewSite().getId());
		this.view = view;
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}
	static class MyTableViewer extends TableViewer {
		public MyTableViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void unmapAllElements() {
			super.unmapAllElements();
		}
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new MyTableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTableViewContentProvider());
		//MagicCardLabelProvider labelProvider = new MagicCardLabelProvider();
		//this.viewer.setLabelProvider(labelProvider);
		this.viewer.setUseHashlookup(true);
		addDargAndDrop();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	public void addDargAndDrop() {
		final Table table = this.viewer.getTable();
		table.setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		this.viewer.addDragSupport(ops, transfers, new MagicCardDragListener(this.viewer, this.view));
		this.viewer.addDropSupport(ops, transfers, new MagicCardDropAdapter(this.viewer, this.view));
	}

	protected void createDefaultColumns() {
		createColumnLabelProviders();
		for (int i = 0; i < getColumnsNumber(); i++) {
			ColumnManager man = (ColumnManager) this.columns.get(i);
			TableViewerColumn colv = new TableViewerColumn(this.viewer, i);
			TableColumn col = colv.getColumn();
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
			col.setMoveable(true);
			colv.setLabelProvider(man);
			if (man instanceof Listener) {
				this.viewer.getTable().addListener(SWT.PaintItem, (Listener) man);
			}
			;
			colv.setEditingSupport(man.getEditingSupport(this.viewer));
		}
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		this.viewer.getTable().setHeaderVisible(true);
	}

	@Override
	protected void updateSortColumn(int index) {
		this.viewer.getTable().setSortColumn(this.viewer.getTable().getColumn(index));
		int sortDirection = this.viewer.getTable().getSortDirection();
		if (sortDirection != SWT.DOWN)
			sortDirection = SWT.DOWN;
		else
			sortDirection = SWT.UP;
		this.viewer.getTable().setSortDirection(sortDirection);
		ColumnManager man = (ColumnManager) this.viewer.getLabelProvider(index);
		this.filter.setSortIndex(man.getSortIndex());
		this.filter.setAscending(sortDirection == SWT.UP);
	}

	protected void setStatus(String string) {
		this.view.setStatus(string);
	}

	@Override
	protected void updateViewer() {
		updateTableHeader();
		long time = System.currentTimeMillis();
		if (this.viewer.getInput() != getFilteredStore()) {
			this.viewer.setInput(getFilteredStore());
			this.viewer.setItemCount(getFilteredStore().getSize());
		} else {
			this.viewer.setSelection(new StructuredSelection());
			this.viewer.getTable().clearAll();
			((MyTableViewer) this.viewer).unmapAllElements();
			this.viewer.setItemCount(getFilteredStore().getSize());
			this.viewer.refresh(true);
		}
		ICardStore cardStore = getFilteredStore().getCardStore();
		String cardCountTotal = "";
		if (cardStore instanceof ICardCountable) {
			cardCountTotal = "Total cards: " + ((ICardCountable) cardStore).getCount();
		}
		setStatus("Shown " + getFilteredStore().getSize() + " items of " + cardStore.getTotal() + ". " + cardCountTotal);
		//System.err.println("set input time: " + (System.currentTimeMillis() - time) + " ms");
	}

	protected void updateTableHeader() {
	}

	@Override
	public void updateColumns(String newValue) {
		TableColumn[] acolumns = this.viewer.getTable().getColumns();
		int order[] = new int[acolumns.length];
		String[] indexes = newValue.split(",");
		if (indexes.length == 0)
			return;
		HashMap used = new HashMap();
		for (int i = 0; i < acolumns.length; i++) {
			TableColumn col = acolumns[i];
			used.put(col, new Integer(i));
		}
		for (int i = 0; i < acolumns.length; i++) {
			try {
				String in = indexes[i];
				int xcol = Integer.parseInt(in);
				int col = xcol > 0 ? xcol - 1 : -xcol - 1;
				TableColumn acol = acolumns[col];
				order[i] = col;
				boolean checked = xcol > 0;
				if (checked) {
					if (acol.getWidth() <= 0)
						acol.setWidth(((ColumnManager) this.columns.get(i)).getColumnWidth());
				} else {
					acol.setWidth(0);
				}
				used.remove(acol);
			} catch (RuntimeException e) {
				TableColumn acol = (TableColumn) used.keySet().iterator().next();
				order[i] = ((Integer) used.get(acol)).intValue();
				used.remove(acol);
			}
		}
		this.viewer.getTable().setColumnOrder(order);
	}
}
