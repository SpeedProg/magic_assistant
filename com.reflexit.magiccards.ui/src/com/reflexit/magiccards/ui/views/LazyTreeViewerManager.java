package com.reflexit.magiccards.ui.views;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeColumn;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;

public class LazyTreeViewerManager extends ViewerManager {
	static class MyTreeViewer extends TreeViewer {
		public MyTreeViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void unmapAllElements() {
			super.unmapAllElements();
		}
	}

	private MyTreeViewer viewer;

	public LazyTreeViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new MyTreeViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTreeViewContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		hookDragAndDrop();
		createDefaultColumns();
		return this.viewer.getControl();
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
			col.setMoveable(true);
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
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	protected String moveGroupOnTop(String value) {
		String newValue = value;
		newValue = newValue.replaceAll("-?" + GroupColumn.COL_NAME + ",", "");
		newValue = GroupColumn.COL_NAME + "," + newValue;
		return newValue;
	}

	@Override
	public void updateColumns(String value) {
		String newValue = moveGroupOnTop(value);
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		int order[] = new int[acolumns.length];
		String[] prefValues = newValue.split(",");
		if (prefValues.length == 0)
			return;
		HashMap<String, Integer> colOrger = new HashMap<String, Integer>();
		HashSet<Integer> orderGaps = new HashSet<Integer>();
		for (int i = 0; i < prefValues.length; i++) {
			Integer integer = Integer.valueOf(i);
			colOrger.put(prefValues[i], integer);
		}
		for (int i = 0; i < order.length; i++) {
			order[i] = -1;
		}
		for (int i = 0; i < acolumns.length; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			boolean checked = true;
			String key = mcol.getColumnFullName();
			Integer pos = colOrger.get(key);
			if (pos == null) {
				pos = colOrger.get("-" + key);
				if (pos != null)
					checked = false;
			}
			if (pos != null) {
				order[pos.intValue()] = i;
			} else {
				orderGaps.add(Integer.valueOf(i)); // i'th column has no
													// position
			}
			if (checked || mcol instanceof GroupColumn) {
				if (acol.getWidth() <= 0)
					acol.setWidth(getColumn(i).getColumnWidth());
			} else {
				acol.setWidth(0);
			}
		}
		// fill order for columns which were not in the properly list
		for (int i = 0; i < order.length; i++) {
			int pos = order[i];
			if (pos < 0) {
				if (orderGaps.size() > 0) {
					Integer next = orderGaps.iterator().next();
					orderGaps.remove(next);
					order[i] = next.intValue();
				}
			}
		}
		this.viewer.getTree().setColumnOrder(order);
	}

	protected void updateGrid() {
		boolean grid = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_GRID);
		this.viewer.getTree().setLinesVisible(grid);
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
			getFilter().setSortField(man.getSortField(), sortDirection == SWT.DOWN);
		} else {
			getFilter().setNoSort();
		}
	}

	@Override
	public void updateViewer() {
		updateTableHeader();
		updateGrid();
		// long time = System.currentTimeMillis();
		// if (this.viewer.getInput() != this.getDataHandler()) {
		if (this.viewer.getInput() != this.getFilteredStore()) {
			this.viewer.unmapAllElements();
			this.viewer.setInput(this.getFilteredStore());
		}
		// System.err.println("set input1 tree time: " +
		// (System.currentTimeMillis() - time) + " ms");
		int size = this.getFilteredStore().getCardGroups().length;
		// System.err.println("size=" + size);
		this.viewer.getTree().setItemCount(size);
		this.viewer.refresh(true);
		// } else {
		// this.viewer.setSelection(new StructuredSelection());
		// this.viewer.getTree().clearAll(true);
		// ((MyTreeViewer) this.viewer).unmapAllElements();
		// this.viewer.refresh(true);
		// }
		// System.err.println("set input2 tree time: " +
		// (System.currentTimeMillis() - time) + " ms");
	}
}
