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

public abstract class ViewerManager implements IMagicColumnViewer {
	private ColumnCollection collumns;
	private IColumnSortAction sortAction;

	protected ViewerManager(String prefPageId) {
		this.collumns = doGetColumnCollection(prefPageId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#createContents(org
	 * .eclipse.swt.widgets.Composite)
	 */
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

	public ColumnCollection getColumnsCollection() {
		return collumns;
	}

	protected int getColumnsNumber() {
		return collumns.getColumnsNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#getControl()
	 */
	public Control getControl() {
		return getViewer().getControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#getSelectionProvider ()
	 */
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
	public abstract ColumnViewer getViewer();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#hookContextMenu(org
	 * .eclipse.jface.action.MenuManager)
	 */
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
	public void hookDoubleClickListener(IDoubleClickListener doubleClickListener) {
		getViewer().addDoubleClickListener(doubleClickListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.IMagicColumnViewer#hookSortAction(com
	 * .reflexit.magiccards.ui.views.IColumnSortAction)
	 */
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
	public void flip(boolean hasGroups) {
		// flip between tree and table if control supports it
	}

	protected void updateGrid() {
		boolean grid = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_GRID);
		setLinesVisible(grid);
		getViewer().getControl().setFont(getFont());
		getViewer().getControl().setForeground(MagicUIActivator.getDefault().getTextColor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.IMagicCardListControl#hookDragAndDrop()
	 */
	public void hookDragAndDrop() {
		getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer()));
		getViewer().addDropSupport(ops, transfers, new MagicCardDropAdapter(getViewer()));
	}

	public abstract int getSortDirection();

	public Font getFont() {
		return MagicUIActivator.getDefault().getFont();
	}
}
