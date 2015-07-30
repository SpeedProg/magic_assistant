package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

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
	private ColumnCollection collumns;
	private IColumnSortAction sortAction;

	protected ViewerManager(String prefPageId) {
		this.collumns = doGetColumnCollection(prefPageId);
	}

	protected ViewerManager(ColumnCollection columns) {
		this.collumns = columns;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#createContents(org
	 * .eclipse.swt.widgets.Composite)
	 */
	@Override
	public abstract Control createContents(Composite parent);

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#dispose()
	 */
	public void dispose() {
		// override to dispose resources
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

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#getControl()
	 */
	@Override
	public Control getControl() {
		return getViewer().getControl();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#getSelectionProvider ()
	 */
	@Override
	public ISelectionProvider getSelectionProvider() {
		return getViewer();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#getShell()
	 */
	public Shell getShell() {
		return getControl().getShell();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#getViewer()
	 */
	@Override
	public abstract ColumnViewer getViewer();

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#hookContextMenu(org
	 * .eclipse.jface.action.MenuManager)
	 */
	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		Menu menu = menuMgr.createContextMenu(getViewer().getControl());
		getViewer().getControl().setMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#hookDoubleClickListener
	 * (org.eclipse.jface.viewers.IDoubleClickListener)
	 */
	@Override
	public void hookDoubleClickListener(IDoubleClickListener doubleClickListener) {
		getViewer().addDoubleClickListener(doubleClickListener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#hookSortAction(com
	 * .reflexit.magiccards.ui.views.IColumnSortAction)
	 */
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

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#flip(boolean)
	 */
	@Override
	public void flip(boolean hasGroups) {
		// flip between tree and table if control supports it
	}

	protected void updateGrid() {
		try {
			boolean grid = MagicUIActivator.getDefault().getPreferenceStore()
					.getBoolean(PreferenceConstants.SHOW_GRID);
			setLinesVisible(grid);
			getViewer().getControl().setFont(getFont());
			getViewer().getControl().setForeground(MagicUIActivator.getDefault().getTextColor());
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public void refresh() {
		if (getViewer().getControl().isDisposed())
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
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		viewer.addDragSupport(ops, transfers, new MagicCardDragListener(viewer));
		viewer.addDropSupport(ops, transfers, new MagicCardDropAdapter(viewer));
	}


	@Override
	public abstract int getSortDirection();

	public Font getFont() {
		return MagicUIActivator.getDefault().getFont();
	}
}
