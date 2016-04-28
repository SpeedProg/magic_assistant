package com.reflexit.magiccards.ui.views;

import java.util.Arrays;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.ViewerManager.IContextMenuFiller;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.model.ISelectionTranslator;
import com.reflexit.magiccards.ui.views.model.LazyTableViewContentProvider;
import com.reflexit.magiccards.ui.views.model.SortOrderViewerComparator;
import com.reflexit.magiccards.ui.views.model.TableViewerContentProvider;

public class ExtendedTableViewer extends TableViewer implements IMagicColumnViewer {
	protected final ViewerManager manager;
	protected int filler = 0;

	protected ExtendedTableViewer(Composite parent, int style) {
		super(parent, style);
		this.manager = new ViewerManager(this, null);
	}

	public ExtendedTableViewer(Composite parent, ColumnCollection collection) {
		this(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.VIRTUAL);
		setColumnCollection(collection);
	}

	public ExtendedTableViewer(Composite parent, String id) {
		this(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.VIRTUAL);
		setColumnCollection(doGetColumnCollection(id));
	}

	@Override
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		super.setLabelProvider(labelProvider);
		if (labelProvider instanceof Listener)
			addPaintListener((Listener) labelProvider);
	}

	protected void setColumnCollection(ColumnCollection collection) {
		manager.setCollumns(collection);
		createContents();
	}

	public void applyColumnProperties() {
		setColumnProperties(getTControl().getColumns());
		int[] order = getTControl().getColumnOrder();
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.setColumnOrder(order);
	}

	@Override
	protected void associate(Object element, Item item) {
		if (element == null)
			return;
		super.associate(element, item);
	}

	protected Menu createColumnHeaderContextMenu(int index) {
		if (index < 0)
			return null;
		final TableColumn column = getTColumn(index);
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
			final MenuItem showColumn = new MenuItem(menu, SWT.CASCADE);
			showColumn.setText("Show Column");
			Menu smenu = new Menu(menu);
			TableColumn[] columns = getTControl().getColumns();
			for (int i = 0; i < columns.length; i++) {
				TableColumn tcolumn = columns[i];
				AbstractColumn man = (AbstractColumn) tcolumn.getData("man");
				if (man == null)
					continue;
				final MenuItem itemShow = new MenuItem(smenu, SWT.PUSH);
				itemShow.setText(man.getColumnFullName());
				itemShow.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						show(tcolumn, man.getUserWidth());
					}
				});
			}
			showColumn.setMenu(smenu);
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
			itemShow.setText("Preferences... ");
			itemShow.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					manager.openColumnPreferences(manager.getPreferencesId());
				}
			});
		}
		return menu;
	}

	protected void createContents() {
		setContentProvider(new TableViewerContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		setUseHashlookup(true);
		IColumnSortAction sortAction = new IColumnSortAction() {
			@Override
			public void sort(int i, int dir) {
				setSortColumn(i, dir);
				refresh(true);
			}
		};
		hookSortAction(sortAction);
		syncViewer();
		createLabelProviders();
		ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
		getTControl().setHeaderVisible(true);
		hookMenuDetect(getTControl());
		hookDragAndDrop();
	}

	protected void createLabelProviders() {
		TableColumn[] columns = this.getTable().getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].dispose();
		}
		// define the menu and assign to the table
		int num = manager.getColumnsNumber();
		for (int i = 0; i < num; i++) {
			AbstractColumn man = manager.getColumn(i);
			TableViewerColumn colv = new TableViewerColumn(this, SWT.LEFT);
			TableColumn col = colv.getColumn();
			col.setData("man", man);
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
				addPaintListener((Listener) man);
			}
			colv.setEditingSupport(man.getEditingSupport(this));
		}
		createFillerColumn();
	}

	public void addPaintListener(Listener man) {
		getTControl().addListener(SWT.EraseItem, man);
		getTControl().addListener(SWT.PaintItem, man);
		getTControl().addListener(SWT.MeasureItem, man);
		getTControl().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				getTControl().removeListener(SWT.EraseItem, man);
				getTControl().removeListener(SWT.PaintItem, man);
				getTControl().removeListener(SWT.MeasureItem, man);
			}
		});
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

	@Override
	public void dispose() {
		getControl().dispose();
		getLabelProvider().dispose();
	}

	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId);
	}

	protected AbstractColumn getColumn(int i) {
		return getColumnsCollection().getColumn(i);
	}

	protected int getColumnIndex(Point pt) {
		int prev = 0;
		int[] order = getTControl().getColumnOrder();
		int x = pt.x;
		for (int j = 0; j < order.length; j++) {
			int i = order[j];
			int w = getTColumn(i).getWidth();
			if (x < prev + w) {
				return i;
			}
			prev += w;
		}
		return -1;
	}

	@Override
	public String getColumnLayoutProperty() {
		applyColumnProperties();
		ColumnCollection columnsCollection = getColumnsCollection();
		return columnsCollection.getColumnLayoutProperty();
	}

	@Override
	public ColumnCollection getColumnsCollection() {
		return manager.getColumnsCollection();
	}

	@Override
	public ColumnViewer getColumnViewer() {
		return this;
	}

	public Font getFont() {
		return ViewerManager.getFont();
	}

	public MenuManager getMenuManager() {
		return manager.getMenuManager();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return this;
	}

	@Override
	public int getSortDirection() {
		return getTControl().getSortDirection();
	}

	protected TableColumn getTColumn(int index) {
		return getTControl().getColumn(index);
	}

	protected Table getTControl() {
		return getTable();
	}

	@Override
	public ColumnViewer getViewer() {
		return this;
	}

	public SortOrderViewerComparator getViewerComparator() {
		return manager.getViewerComparator();
	}

	protected void hide(final TableColumn column) {
		column.setWidth(0);
		column.setResizable(false);
	}

	@Override
	public void hookContext(String id) {
		manager.hookContext(id);
	}

	@Override
	public boolean hookContextMenu(MenuManager menuMgr) {
		return manager.hookContextMenu(menuMgr);
	}

	protected MenuManager hookContextMenu(final IContextMenuFiller filler) {
		return manager.hookContextMenu(filler);
	}

	@Override
	public void hookDragAndDrop() {
		manager.hookDragAndDrop();
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
					MenuManager menuManager = getMenuManager();
					if (menuManager != null) {
						Menu menu = menuManager.createContextMenu(tcontrol);
						tcontrol.setMenu(menu);
					}
				}
			}
		});
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		manager.hookSortAction(sortAction);
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		if (getControl().isDisposed())
			return;
		super.inputChanged(input, oldInput);
		syncViewer();
	}

	@Override
	public void refresh() {
		super.refresh();
		syncViewer();
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

	protected void setControlSortColumn(int index, int sortDirection) {
		getTControl().setSortColumn(index >= 0 ? getTColumn(index) : null);
		getTControl().setSortDirection(sortDirection);
	}

	@Override
	public void setLinesVisible(boolean grid) {
		getTControl().setLinesVisible(grid);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		if (!(selection instanceof IStructuredSelection))
			return;
		IContentProvider contentProvider = getContentProvider();
		if (contentProvider instanceof LazyTableViewContentProvider) {
			LazyTableViewContentProvider provider = (LazyTableViewContentProvider) contentProvider;
			int[] indices = provider.getIndices((IStructuredSelection) selection);
			getTable().setSelection(indices);
			getTable().showSelection();
		} else if (contentProvider instanceof ISelectionTranslator) {
			super.setSelection(
					((ISelectionTranslator) contentProvider).translateSelection((IStructuredSelection) selection, 1),
					reveal);
		} else {
			super.setSelection(selection, reveal);
		}
	}

	@Override
	public void setSortColumn(int index, int direction) {
		int sortDirection = SWT.NONE;
		if (index >= 0) {
			if (direction == 0) {
				sortDirection = getSortDirection();
				if (sortDirection != SWT.DOWN)
					sortDirection = SWT.DOWN;
				else
					sortDirection = SWT.UP;
			} else if (direction == 1)
				sortDirection = SWT.DOWN;
			else if (direction == -1)
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

	protected void show(final TableColumn acol, int width) {
		if (width < 16)
			width = 16; // min reasonable width
		if (width > 500)
			width = 500;
		acol.setWidth(width);
		acol.setResizable(true);
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
				show(acol, w);
			} else {
				hide(acol);
			}
		}
	}

	protected void syncViewer() {
		try {
			boolean grid = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_GRID);
			setLinesVisible(grid);
			getControl().setFont(MagicUIActivator.getDefault().getFont());
			getControl().setForeground(MagicUIActivator.getDefault().getTextColor());
		} catch (Exception e) {
			// ignore
		}
	}
}