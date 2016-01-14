package com.reflexit.magiccards.ui.views;

import java.util.Arrays;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.ViewerManager.IContextMenuFiller;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.model.ISelectionTranslator;
import com.reflexit.magiccards.ui.views.model.SortOrderViewerComparator;
import com.reflexit.magiccards.ui.views.model.TreeViewerContentProvider;

public class ExtendedTreeViewer extends TreeViewer implements IMagicColumnViewer, ISelectionTranslator {
	protected final ViewerManager manager;
	private int filler = 0;

	protected ExtendedTreeViewer(Composite parent) {
		this(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER | SWT.VIRTUAL);
	}

	public ExtendedTreeViewer(Composite parent, ColumnCollection collection) {
		this(parent);
		setColumnCollection(collection);
	}

	protected ExtendedTreeViewer(Composite parent, int style) {
		super(parent, style);
		this.manager = new ViewerManager(this, null);
	}

	public ExtendedTreeViewer(Composite parent, String id) {
		this(parent);
		setColumnCollection(doGetColumnCollection(id));
	}

	protected void setColumnCollection(ColumnCollection collection) {
		manager.setCollumns(collection);
		createContents();
	}

	@Override
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		if (labelProvider != null) {
			super.setLabelProvider(labelProvider);
			if (labelProvider instanceof Listener)
				addPaintListener((Listener) labelProvider);
		}
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
		final TreeColumn column = getTColumn(index);
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
			TreeColumn[] columns = getTControl().getColumns();
			for (int i = 0; i < columns.length; i++) {
				TreeColumn tcolumn = columns[i];
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
					manager.callSortAction(index, 1);
				}
			});
			final MenuItem itemSortD = new MenuItem(menu, SWT.PUSH);
			itemSortD.setText("Sort Descending: " + name);
			itemSortD.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					manager.callSortAction(index, -1);
				}
			});
			final MenuItem itemSortUnsort = new MenuItem(menu, SWT.PUSH);
			itemSortUnsort.setText("Unsort");
			itemSortUnsort.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					manager.callSortAction(-1, 0);
				}
			});
			final MenuItem itemShow = new MenuItem(menu, SWT.PUSH);
			itemShow.setText("Column Preferences... ");
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
		setContentProvider(new TreeViewerContentProvider());
		setAutoExpandLevel(2);
		setUseHashlookup(true);
		updatePresentation();
		createLabelProviders();
		ColumnViewerToolTipSupport.enableFor(getTViewer(), ToolTip.NO_RECREATE);
		getTControl().setHeaderVisible(true);
		hookMenuDetect(getTControl());
		hookDragAndDrop();
	}

	protected void createLabelProviders() {
		int num = manager.getColumnsNumber();
		for (int i = 0; i < num; i++) {
			createColumn(i);
		}
		createFillerColumn();
	}

	protected TreeViewerColumn createColumn(int i) {
		AbstractColumn man = manager.getColumn(i);
		TreeViewerColumn colv = new TreeViewerColumn(getTViewer(), i);
		TreeColumn col = colv.getColumn();
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
		colv.setEditingSupport(man.getEditingSupport(getTViewer()));
		return colv;
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
		TreeViewerColumn colv = new TreeViewerColumn(getTViewer(), SWT.LEFT);
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
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getControl().getShell(), id,
							new String[] { id }, null);
					dialog.open();
				}
			});
		}
	}

	@Override
	public void dispose() {
		getLabelProvider().dispose();
		getControl().dispose();
	}

	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId);
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

	public StructuredViewer getStructuredViewer() {
		return this;
	}

	protected TreeColumn getTColumn(int index) {
		return getTControl().getColumn(index);
	}

	protected Tree getTControl() {
		return getTree();
	}

	public TreeViewer getTViewer() {
		return this;
	}

	@Override
	public Viewer getViewer() {
		return this;
	}

	public SortOrderViewerComparator getViewerComparator() {
		return manager.getViewerComparator();
	}

	protected void hide(final TreeColumn column) {
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
		hookDragAndDrop(this);
	}

	public void hookDragAndDrop(StructuredViewer viewer) {
		ViewerManager.hookDragAndDrop(viewer);
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
		super.inputChanged(input, oldInput);
		updatePresentation();
	}

	@Override
	public void refresh() {
		super.refresh();
		updatePresentation();
	}

	public void setColumnProperties(TreeColumn[] acolumns) {
		for (int i = 0; i < acolumns.length - 1; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = manager.getColumn(i);
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
	public void setSelection(ISelection selection) {
		setSelection(selection, true);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		if (!(selection instanceof IStructuredSelection))
			return;
		super.setSelection(translateSelection((IStructuredSelection) selection, -1), reveal);
	}

	@Override
	public IStructuredSelection translateSelection(IStructuredSelection selection, int level) {
		IContentProvider contentProvider = getContentProvider();
		if (contentProvider instanceof ISelectionTranslator) {
			selection = ((ISelectionTranslator) contentProvider).translateSelection(selection, level);
		}
		return selection;
	}

	@Override
	public void setLinesVisible(boolean grid) {
		getTControl().setLinesVisible(grid);
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

	public void showColumn(int i, boolean show) {
		TreeColumn[] acolumns = getTControl().getColumns();
		TreeColumn column = acolumns[i];
		if (!show) {
			hide(column);
		} else {
			int def = manager.getColumn(i).getColumnWidth();
			show(column, def);
		}
	}

	protected void show(final TreeColumn acol, int width) {
		if (width < 16)
			width = 16; // min reasonable width
		if (width > 500)
			width = 500;
		acol.setWidth(width);
		acol.setResizable(true);
	}

	public boolean supportsGroupping(boolean groupped) {
		if (groupped)
			return true;
		return false;
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
		getTControl().setColumnOrder(columnsOrder);
		TreeColumn[] acolumns = getTree().getColumns();
		for (int i = 0; i < acolumns.length - filler; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = manager.getColumn(i);
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
	public void updateColumns(String value) {
		getColumnsCollection().updateColumnsFromPropery(value);
		syncColumns();
	}

	protected void updatePresentation() {
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