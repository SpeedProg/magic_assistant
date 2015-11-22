package com.reflexit.magiccards.ui.views;

import java.util.Arrays;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.SortOrder;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class ExtendedTableViewer extends TableViewer implements IMagicColumnViewer {
	protected ViewerManager manager;
	protected SortOrder sortOrder = new SortOrder();
	protected int filler = 0;

	protected ExtendedTableViewer(Composite parent, int style) {
		super(parent, style);
		getControl().setFont(MagicUIActivator.getDefault().getFont());
	}

	private ExtendedTableViewer(Composite parent) {
		super(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
	}

	public ExtendedTableViewer(Composite parent, String id) {
		this(parent);
		ColumnCollection collection = doGetColumnCollection(id);
		init(collection);
	}

	public ExtendedTableViewer(Composite parent, ColumnCollection collection) {
		this(parent);
		init(collection);
	}

	private void init(ColumnCollection collection) {
		this.manager = new ViewerManager(collection) {
			@Override
			public Viewer getViewer() {
				return ExtendedTableViewer.this;
			}
		};
		createContents();
	}

	protected void createContents() {
		setContentProvider(new TableViewerContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		setUseHashlookup(true);
		IColumnSortAction sortAction = new IColumnSortAction() {
			@Override
			public void sort(int i, int dir) {
				updateSortColumn(i, dir);
				refresh(true);
			}
		};
		hookSortAction(sortAction);
		updatePresentation();
		createDefaultColumns();
	}

	protected void updatePresentation() {
		try {
			boolean grid = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_GRID);
			setLinesVisible(grid);
			getControl().setFont(getFont());
			getControl().setForeground(MagicUIActivator.getDefault().getTextColor());
		} catch (Exception e) {
			// ignore
		}
	}

	protected void updateSortColumn(final int index, int dir) {
		if (index >= 0) {
			AbstractColumn man = (AbstractColumn) getViewer().getLabelProvider(index);
			if (man != null) {
				ICardField sortField = man.getSortField();
				if (sortField == null)
					sortField = MagicCardField.NAME;
				boolean acc = true;
				if (dir == 0) {
					boolean oldAcc = sortOrder.isAccending(sortField);
					acc = !oldAcc;
				} else {
					acc = dir == 1 ? true : false;
				}
				sortOrder.setSortField(sortField, acc);
				setSortColumn(index, acc ? 1 : -1);
			} else {
				setSortColumn(index, dir);
			}
		} else {
			setSortColumn(-1, 0);
			sortOrder.clear();
		}
	}

	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId);
	}

	@Override
	public void unmapAllElements() {
		super.unmapAllElements();
	}

	@Override
	protected void associate(Object element, Item item) {
		if (element == null)
			return;
		super.associate(element, item);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		IContentProvider contentProvider = getContentProvider();
		if (selection instanceof IStructuredSelection && contentProvider instanceof LazyTableViewContentProvider) {
			LazyTableViewContentProvider provider = (LazyTableViewContentProvider) contentProvider;
			int[] indices = provider.getIndices((IStructuredSelection) selection);
			getTable().setSelection(indices);
			getTable().showSelection();
		} else {
			super.setSelection(selection, reveal);
		}
	}

	@Override
	public ColumnCollection getColumnsCollection() {
		return manager.getColumnsCollection();
	}

	@Override
	public void dispose() {
		getControl().dispose();
		getLabelProvider().dispose();
	}

	@Override
	public void hookContext(String id) {
		manager.hookContext(id);
	}

	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		manager.hookContextMenu(menuMgr);
	}

	public MenuManager getMenuManager() {
		return manager.getMenuManager();
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		manager.hookSortAction(sortAction);
	}

	@Override
	public void refresh() {
		super.refresh();
		updatePresentation();
	}

	@Override
	public void hookDragAndDrop() {
		manager.hookDragAndDrop();
	}

	public Font getFont() {
		return ViewerManager.getFont();
	}

	@Override
	public ColumnViewer getViewer() {
		return this;
	}

	protected Table getTControl() {
		return getTable();
	}

	@Override
	public void updateColumns(String value) {
		if (getTControl().isDisposed())
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
			AbstractColumn mcol = manager.getColumn(i);
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

	protected void hide(final Item column) {
		((TableColumn) column).setWidth(0);
		((TableColumn) column).setResizable(false);
	}

	protected Item getTColumn(int index) {
		return getTControl().getColumn(index);
	}

	protected void setControlSortColumn(int index, int sortDirection) {
		getTControl().setSortColumn((TableColumn) (index >= 0 ? getTColumn(index) : null));
		getTControl().setSortDirection(sortDirection);
	}

	@Override
	public void setSortColumn(int index, int direction) {
		int sortDirection = getSortDirection();
		if (index >= 0) {
			if (direction == 0) {
				if (sortDirection != SWT.DOWN)
					sortDirection = SWT.DOWN;
				else
					sortDirection = SWT.UP;
			} else if (direction == 1)
				sortDirection = SWT.DOWN;
			else
				sortDirection = SWT.UP;
		}
		setControlSortColumn(index, sortDirection);
		// if (index >= 0) {
		// AbstractColumn man = (AbstractColumn)
		// getColumnViewer().getLabelProvider(index);
		// vcomp.setOrder(man.getSortField(), sortDirection == SWT.UP);
		// getColumnViewer().setComparator(vcomp);
		// } else {
		// getColumnViewer().setComparator(null);
		// }
	}

	public SortOrderViewerComparator getViewerComparator() {
		return manager.getViewerComparator();
	}

	@Override
	public String getColumnLayoutProperty() {
		applyColumnProperties();
		ColumnCollection columnsCollection = getColumnsCollection();
		return columnsCollection.getColumnLayoutProperty();
	}

	public void applyColumnProperties() {
		setColumnProperties(getTControl().getColumns());
		int[] order = getTControl().getColumnOrder();
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.setColumnOrder(order);
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

	protected AbstractColumn getColumn(int i) {
		return getColumnsCollection().getColumn(i);
	}

	@Override
	public void setLinesVisible(boolean grid) {
		getTControl().setLinesVisible(grid);
	}

	@Override
	public int getSortDirection() {
		return getTControl().getSortDirection();
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		if (getControl().isDisposed())
			return;
		super.inputChanged(input, oldInput);
		updatePresentation();
	}

	public boolean supportsGroupping(boolean groupped) {
		if (groupped)
			return false;
		return true;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return this;
	}

	@Override
	public ColumnViewer getColumnViewer() {
		return this;
	}

	protected void createDefaultColumns() {
		// define the menu and assign to the table
		int num = manager.getColumnsNumber();
		for (int i = 0; i < num; i++) {
			AbstractColumn man = manager.getColumn(i);
			TableViewerColumn colv = new TableViewerColumn(this, SWT.LEFT);
			TableColumn col = colv.getColumn();
			col.setText(man.getColumnName());
			col.setWidth(man.getUserWidth());
			col.setToolTipText(man.getColumnTooltip());
			final int coln = i;
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					manager.callSortAction(coln, 0);
				}
			});
			col.setMoveable(true);
			colv.setLabelProvider(man);
			if (man instanceof Listener) {
				getTControl().addListener(SWT.EraseItem, (Listener) man);
				getTControl().addListener(SWT.PaintItem, (Listener) man);
				getTControl().addListener(SWT.MeasureItem, (Listener) man);
			}
			colv.setEditingSupport(man.getEditingSupport(this));
		}
		createFillerColumn();
		ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
		getTControl().setHeaderVisible(true);
		hookMenuDetect(getTControl());
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

	protected void createFillerColumn() {
		filler = 1;
		TableViewerColumn colv = new TableViewerColumn(this, SWT.LEFT);
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
		String id = manager.getPreferencesId();
		if (id != null) {
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					manager.openColumnPreferences(id);
				}
			});
		}
	}

	protected Menu createColumnHeaderContextMenu(int index) {
		if (index < 0)
			return null;
		final Item column = getTColumn(index);
		Menu menu = new Menu(getControl());
		String name = column.getText();
		if (!name.isEmpty()) {
			final MenuItem itemHide = new MenuItem(menu, SWT.PUSH);
			itemHide.setText("Hide Column: " + name);
			itemHide.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					hide(column);
				}
			});
			final MenuItem itemSort = new MenuItem(menu, SWT.PUSH);
			itemSort.setText("Sort Accending: " + name);
			itemSort.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setSortColumn(index, 1);
				}
			});
			final MenuItem itemSortD = new MenuItem(menu, SWT.PUSH);
			itemSortD.setText("Sort Descending: " + name);
			itemSortD.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setSortColumn(index, -1);
				}
			});
			final MenuItem itemSortUnsort = new MenuItem(menu, SWT.PUSH);
			itemSortUnsort.setText("Unsort");
			itemSortUnsort.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setSortColumn(-1, 0);
				}
			});
			final MenuItem itemShow = new MenuItem(menu, SWT.PUSH);
			itemShow.setText("Show Column... ");
			itemShow.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					manager.openColumnPreferences(manager.getPreferencesId());
				}
			});
		}
		return menu;
	}
}