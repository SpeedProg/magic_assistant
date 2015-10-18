package com.reflexit.magiccards.ui.views;

import java.util.Arrays;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;

public class TreeViewerManager extends ViewerManager {
	protected TreeViewer viewer;
	private int filler;

	public TreeViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		this.viewer.getTree().setFont(getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new TreeViewContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	@Override
	public void dispose() {
		this.viewer.getLabelProvider().dispose();
		this.viewer.getControl().dispose();
		this.viewer = null;
	}

	protected void createDefaultColumns() {
		// define the menu and assign to the table
		int num = getColumnsNumber();
		for (int i = 0; i < num; i++) {
			AbstractColumn man = getColumn(i);
			TreeViewerColumn colv = new TreeViewerColumn(this.viewer, i);
			TreeColumn col = colv.getColumn();
			col.setText(man.getColumnName());
			col.setWidth(man.getUserWidth());
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
				getTControl().addListener(SWT.EraseItem, (Listener) man);
				getTControl().addListener(SWT.PaintItem, (Listener) man);
				getTControl().addListener(SWT.MeasureItem, (Listener) man);
			}
			colv.setEditingSupport(man.getEditingSupport(this.viewer));
		}
		createFillerColumn();
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		getTControl().setHeaderVisible(true);
		hookMenuDetect(getTControl());
	}

	private Tree getTControl() {
		return this.viewer.getTree();
	}

	protected void hookMenuDetect(Tree tcontrol) {
		tcontrol.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Point ptm = new Point(event.x, event.y);
				Point pt = tcontrol.getDisplay().map(null, tcontrol, ptm);
				Rectangle clientArea = tcontrol.getClientArea();
				boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + tcontrol.getHeaderHeight());
				Menu oldMenu = tcontrol.getMenu();
				if (oldMenu != null && !oldMenu.isDisposed()) {
					oldMenu.dispose();
				}
				if (header) {
					int columnIndex = getColumnIndex(pt);
					tcontrol.setMenu(createColumnHeaderContextMenu(columnIndex));
				} else {
					Menu menu = getMenuManager().createContextMenu(tcontrol);
					tcontrol.setMenu(menu);
				}
			}
		});
	}

	protected void createFillerColumn() {
		filler = 1;
		TreeViewerColumn colv = new TreeViewerColumn(this.viewer, SWT.LEFT);
		TreeColumn col = colv.getColumn();
		col.setText("");
		col.setWidth(16);
		col.setToolTipText("This is just a filler");
		col.setMoveable(false);
		colv.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				// empty
			}
		});
		String id = getColumnsCollection().getId();
		if (id != null) {
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id,
							new String[] { id }, null);
					dialog.open();
				}
			});
		}
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	@Override
	public void updateColumns(String value) {
		getColumnsCollection().updateColumnsFromPropery(value);
		syncColumns();
	}

	protected void syncColumns() {
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.moveColumnOnTop(columnsCollection.getColumn(GroupColumn.COL_NAME));
		int[] columnsOrder = getColumnsCollection().getColumnsOrder();
		if (filler == 1) {
			int length = columnsOrder.length;
			columnsOrder = Arrays.copyOf(columnsOrder, length + 1);
			columnsOrder[length] = length; // last column
		}
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		for (int i = 0; i < acolumns.length - filler; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			boolean visible = mcol.isVisible();
			if (visible || mcol instanceof GroupColumn) {
				int w = mcol.getUserWidth();
				if (w < 16)
					w = 16; // min reasonable width
				if (w > 500)
					w = 500;
				if (acol.getWidth() != w) {
					acol.setWidth(w);
				}
			} else {
				acol.setWidth(0);
			}
		}
	}

	@Override
	public String getColumnLayoutProperty() {
		ColumnCollection columnsCollection = getColumnsCollection();
		setColumnProperties(viewer.getTree().getColumns());
		int[] order = viewer.getTree().getColumnOrder();
		columnsCollection.setColumnOrder(Arrays.copyOf(order, order.length - 1));
		return columnsCollection.getColumnLayoutProperty();
	}

	public void setColumnProperties(TreeColumn[] acolumns) {
		for (int i = 0; i < acolumns.length - 1; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			int w = acol.getWidth();
			if (w > 0) {
				mcol.setUserWidth(w);
				mcol.setVisible(true);
			} else
				mcol.setVisible(false);
		}
	}

	@Override
	public void setLinesVisible(boolean grid) {
		this.viewer.getTree().setLinesVisible(grid);
	}

	@Override
	protected void setControlSortColumn(int index, int sortDirection) {
		getTControl().setSortColumn((TreeColumn) (index >= 0 ? getTColumn(index) : null));
		getTControl().setSortDirection(sortDirection);
	}

	@Override
	public int getSortDirection() {
		return this.viewer.getTree().getSortDirection();
	}

	@Override
	public void updateViewer(Object input) {
		if (viewer == null || this.viewer.getControl().isDisposed())
			return;
		updateTableHeader();
		updateGrid();
		// long time = System.currentTimeMillis();
		// if (this.viewer.getInput() != this.getDataHandler()) {
		// if (this.viewer.getInput() != input)
		{
			this.viewer.setInput(input);
		}
		this.viewer.refresh(true);
	}

	protected void showColumn(int i, boolean show) {
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		TreeColumn column = acolumns[i];
		if (!show) {
			hide(column);
		} else {
			if (column.getWidth() < 16) {
				int def = getColumn(i).getColumnWidth();
				column.setWidth(def);
			}
			column.setResizable(true);
		}
	}

	@Override
	protected int getColumnIndex(Point pt) {
		int prev = 0;
		int[] order = getTControl().getColumnOrder();
		int x = pt.x;
		for (int j = 0; j < order.length; j++) {
			int i = order[j];
			int w = getTControl().getColumn(i).getWidth();
			if (x < prev + w) {
				return i;
			}
			prev += w;
		}
		return -1;
	}

	@Override
	protected Item getTColumn(int index) {
		return getTControl().getColumn(index);
	}

	@Override
	protected void hide(final Item column) {
		((TreeColumn) column).setWidth(0);
		((TreeColumn) column).setResizable(false);
	}

	@Override
	public boolean supportsGroupping(boolean groupped) {
		if (groupped)
			return true;
		return false;
	};
}
