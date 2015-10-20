package com.reflexit.magiccards.ui.views;

import java.util.Arrays;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.SortOrder;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class TableViewerManager extends ViewerManager {
	protected TableViewer viewer;
	protected SortOrder sortOrder = new SortOrder();
	private int filler = 0;

	public TableViewerManager(String id) {
		super(id);
	}

	public TableViewerManager(ColumnCollection columns) {
		super(columns);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		getTControl().setFont(getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new TableViewerContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		IColumnSortAction sortAction = new IColumnSortAction() {
			@Override
			public void sort(int i) {
				TableViewerManager.this.updateSortColumn(i);
				viewer.refresh(true);
			}
		};
		hookSortAction(sortAction);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	@Override
	public void dispose() {
		this.viewer.getControl().dispose();
		this.viewer = null;
	}

	@Override
	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return getColumnsCollection();
	}

	protected void createDefaultColumns() {
		// define the menu and assign to the table
		int num = getColumnsNumber();
		for (int i = 0; i < num; i++) {
			AbstractColumn man = getColumn(i);
			TableViewerColumn colv = new TableViewerColumn(this.viewer, SWT.LEFT);
			TableColumn col = colv.getColumn();
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

	private Table getTControl() {
		return this.viewer.getTable();
	}

	protected void hookMenuDetect(Table tcontrol) {
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
		TableViewerColumn colv = new TableViewerColumn(this.viewer, SWT.LEFT);
		TableColumn col = colv.getColumn();
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
		String id = getPreferencesId();
		if (id != null) {
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					openColumnPreferences(id);
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
		if (this.viewer == null || getTControl() == null || getTControl().isDisposed())
			return;
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.updateColumnsFromPropery(value);
		int[] columnsOrder = columnsCollection.getColumnsOrder();
		if (filler == 1) {
			int length = columnsOrder.length;
			columnsOrder = Arrays.copyOf(columnsOrder, length + 1);
			columnsOrder[length] = length; // last column
		}
		getTControl().setColumnOrder(columnsOrder);
		TableColumn[] acolumns = getTControl().getColumns();
		for (int i = 0; i < acolumns.length - filler; i++) {
			TableColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			boolean visible = mcol.isVisible();
			if (visible) {
				int w = mcol.getUserWidth();
				if (w < 16)
					w = 16; // min reasonable width
				if (w > 500)
					w = 500;
				acol.setWidth(w);
				acol.setResizable(true);
			} else {
				hide(acol);
			}
		}
	}

	@Override
	public String getColumnLayoutProperty() {
		ColumnCollection columnsCollection = getColumnsCollection();
		setColumnProperties(viewer.getTable().getColumns());
		int[] order = viewer.getTable().getColumnOrder();
		columnsCollection.setColumnOrder(Arrays.copyOf(order, order.length - 1));
		return columnsCollection.getColumnLayoutProperty();
	}

	public void setColumnProperties(TableColumn[] acolumns) {
		for (int i = 0; i < acolumns.length - 1; i++) {
			TableColumn acol = acolumns[i];
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
		getTControl().setLinesVisible(grid);
	}

	protected void updateSortColumn(final int index) {
		if (index >= 0) {
			AbstractColumn man = (AbstractColumn) getViewer().getLabelProvider(index);
			ICardField sortField = man.getSortField();
			if (sortField == null)
				sortField = MagicCardField.NAME;
			boolean acc = true;
			if (sortOrder.isTop(sortField)) {
				boolean oldAcc = sortOrder.isAccending(sortField);
				acc = !oldAcc;
			}
			sortOrder.setSortField(sortField, acc);
			setSortColumn(index, acc ? 1 : -1);
		} else {
			setSortColumn(-1, 0);
			sortOrder.clear();
		}
	}

	@Override
	protected void setControlSortColumn(int index, int sortDirection) {
		getTControl().setSortColumn((TableColumn) (index >= 0 ? getTColumn(index) : null));
		getTControl().setSortDirection(sortDirection);
	}

	@Override
	public int getSortDirection() {
		return getTControl().getSortDirection();
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
		// long time = System.currentTimeMillis();
		// if (this.viewer.getInput() != this.getDataHandler()) {
		// if (this.viewer.getInput() != input)
		{
			this.viewer.setInput(input);
		}
		this.viewer.refresh(true);
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
		((TableColumn) column).setWidth(0);
		((TableColumn) column).setResizable(false);
	}

	@Override
	public boolean supportsGroupping(boolean groupped) {
		if (groupped)
			return false;
		return true;
	};
}
