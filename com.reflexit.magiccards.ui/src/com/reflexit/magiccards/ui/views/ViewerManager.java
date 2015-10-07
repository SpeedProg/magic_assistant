package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.PluginTransfer;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.widgets.ContextFocusListener;

public abstract class ViewerManager implements IMagicColumnViewer {
	private SortOrderViewerComparator vcomp = new SortOrderViewerComparator();
	private ColumnCollection collumns;
	private IColumnSortAction sortAction;
	private MenuManager menuManager;

	protected ViewerManager(String prefPageId) {
		this.collumns = doGetColumnCollection(prefPageId);
	}

	protected ViewerManager(ColumnCollection columns) {
		this.collumns = columns;
	}

	@Override
	public abstract Control createContents(Composite parent);

	@Override
	public void dispose() {
		// override to dispose resources
		if (menuManager != null)
			menuManager.dispose();
	}

	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId);
	}

	protected AbstractColumn getColumn(int i) {
		return collumns.getColumn(i);
	}

	@Override
	public ColumnCollection getColumnsCollection() {
		return collumns;
	}

	protected int getColumnsNumber() {
		return collumns.getColumnsNumber();
	}

	@Override
	public void hookContext(String id) {
		getViewer().getControl().addFocusListener(new ContextFocusListener(id));
	}

	@Override
	public Control getControl() {
		return getViewer().getControl();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return getViewer();
	}

	public Shell getShell() {
		return getControl().getShell();
	}

	@Override
	public abstract ColumnViewer getViewer();

	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		this.menuManager = menuMgr;
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	@Override
	public void hookDoubleClickListener(IDoubleClickListener doubleClickListener) {
		getViewer().addDoubleClickListener(doubleClickListener);
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		this.sortAction = sortAction;
	}

	protected void sortColumn(final int coln) {
		if (sortAction != null)
			sortAction.sort(coln);
	}

	protected void updateTableHeader() {
		// to be overriden
	}

	@Override
	public void flip(boolean hasGroups) {
		// flip between tree and table if control supports it
	}

	protected void updateGrid() {
		try {
			boolean grid = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_GRID);
			setLinesVisible(grid);
			getViewer().getControl().setFont(getFont());
			getViewer().getControl().setForeground(MagicUIActivator.getDefault().getTextColor());
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public void refresh() {
		if (getViewer() == null || getViewer().getControl().isDisposed())
			return;
		updateTableHeader();
		updateGrid();
		getViewer().refresh(true);
	}

	@Override
	public void hookDragAndDrop() {
		ColumnViewer viewer = getViewer();
		viewer.getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(ops, new Transfer[] { MagicCardTransfer.getInstance(), TextTransfer.getInstance(),
				PluginTransfer.getInstance() }, new MagicCardDragListener(viewer));
		viewer.addDropSupport(ops, new Transfer[] { MagicCardTransfer.getInstance(), PluginTransfer.getInstance() },
				new MagicCardDropAdapter(viewer));
	}

	@Override
	public abstract int getSortDirection();

	public Font getFont() {
		return MagicUIActivator.getDefault().getFont();
	}

	protected void openColumnPreferences(String id) {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null);
		dialog.open();
	}

	protected String getPreferencesId() {
		return getColumnsCollection().getId();
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
		if (index >= 0) {
			AbstractColumn man = (AbstractColumn) getViewer().getLabelProvider(index);
			vcomp.setOrder(man.getSortField(), sortDirection == SWT.UP);
			getViewer().setComparator(vcomp);
		} else {
			getViewer().setComparator(null);
		}
	}

	protected Item getTColumn(int index) {
		Control control = getControl();
		if (control instanceof Table)
			return ((Table) control).getColumn(index);
		else if (control instanceof Tree)
			return ((Tree) control).getColumn(index);
		return null;
	}

	protected Menu createColumnHeaderContextMenu(int index) {
		if (index < 0)
			return null;
		final Item column = getTColumn(index);
		Menu menu = new Menu(getControl());
		final MenuItem itemShow = new MenuItem(menu, SWT.PUSH);
		itemShow.setText("Show Column... ");
		itemShow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openColumnPreferences(getPreferencesId());
			}
		});
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
			itemSort.setText("Sort Accending");
			itemSort.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setSortColumn(index, 1);
				}
			});
			final MenuItem itemSortD = new MenuItem(menu, SWT.PUSH);
			itemSortD.setText("Sort Descending");
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
		}
		return menu;
	}

	protected void hookMenuDetect(Table table) {
		table.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Point ptm = new Point(event.x, event.y);
				Point pt = table.getDisplay().map(null, table, ptm);
				Rectangle clientArea = table.getClientArea();
				boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + table.getHeaderHeight());
				Menu oldMenu = table.getMenu();
				if (oldMenu != null && !oldMenu.isDisposed()) {
					oldMenu.dispose();
				}
				if (header) {
					int columnIndex = getColumnIndex(pt);
					table.setMenu(createColumnHeaderContextMenu(columnIndex));
				} else {
					Menu menu = getMenuManager().createContextMenu(table);
					table.setMenu(menu);
				}
			}
		});
	}

	protected void setControlSortColumn(int index, int sortDirection) {
		throw new UnsupportedOperationException();
	}

	protected int getColumnIndex(Point pt) {
		throw new UnsupportedOperationException();
	}

	protected void hide(final Item column) {
		throw new UnsupportedOperationException();
	};
}
