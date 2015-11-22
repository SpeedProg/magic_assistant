package com.reflexit.magiccards.ui.views;

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
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class TreeViewerManager extends ViewerManager {
	protected TreeViewer viewer;
	int filler;

	public TreeViewerManager(TreeViewer viewer, String id) {
		super(id);
		createContents(viewer);
	}

	public TreeViewerManager(TreeViewer viewer, ColumnCollection columns) {
		super(columns);
		createContents(viewer);
	}

	protected void createContents(TreeViewer viewer) {
		this.viewer = viewer;
		viewer.getTree().setFont(getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		viewer.setContentProvider(new TreeViewContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		viewer.setUseHashlookup(true);
		updatePresentation();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
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
					callSortAction(coln, 0);
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

	public ColumnViewer getColumnViewer() {
		return getViewer();
	};



	public void applyColumnProperties() {
		setColumnProperties(getTControl().getColumns());
		int[] order = getTControl().getColumnOrder();
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.setColumnOrder(order);
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

	protected void setControlSortColumn(int index, int sortDirection) {
		getTControl().setSortColumn((TreeColumn) (index >= 0 ? getTColumn(index) : null));
		getTControl().setSortDirection(sortDirection);
	}

	protected void updatePresentation() {
		try {
			boolean grid = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_GRID);
			getTViewer().setLinesVisible(grid);
			getViewer().getControl().setFont(getFont());
			getViewer().getControl().setForeground(MagicUIActivator.getDefault().getTextColor());
		} catch (Exception e) {
			// ignore
		}
	}

	public int getSortDirection() {
		return this.viewer.getTree().getSortDirection();
	}

	public void showColumn(int i, boolean show) {
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
					openColumnPreferences(getPreferencesId());
				}
			});
		}
		return menu;
	}

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

	protected Item getTColumn(int index) {
		return getTControl().getColumn(index);
	}


	protected void hide(final Item column) {
		((TreeColumn) column).setWidth(0);
		((TreeColumn) column).setResizable(false);
	}


	public boolean supportsGroupping(boolean groupped) {
		if (groupped)
			return true;
		return false;
	};

	public IMagicViewer getTViewer() {
		if (viewer instanceof IMagicViewer)
			return (IMagicViewer) viewer;
		return null;
	}
}
