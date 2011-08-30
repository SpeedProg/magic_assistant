package com.reflexit.magiccards.ui.views;

import java.util.Collection;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public abstract class ViewerManager implements IDisposable {
	public interface IColumnSortAction {
		void sort(int i);
	}

	private ColumnCollection col;
	private MagicCardFilter filter;
	private IFilteredCardStore<ICard> fstore;
	private IColumnSortAction sortAction;

	protected ViewerManager(String viewId) {
		this.col = doGetColumnCollection(viewId);
		this.filter = new MagicCardFilter();
	}

	public abstract Control createContents(Composite parent);

	public void dispose() {
		// override to dispose resources
	}

	protected ColumnCollection doGetColumnCollection(String viewId) {
		return new MagicColumnCollection(viewId);
	}

	protected AbstractColumn getColumn(int i) {
		return col.getColumn(i);
	}

	public Collection<AbstractColumn> getColumns() {
		return col.getColumns();
	}

	public ColumnCollection getColumnsCollection() {
		return col;
	}

	protected int getColumnsNumber() {
		return col.getColumnsNumber();
	}

	public Control getControl() {
		return getViewer().getControl();
	}

	/**
	 * @return
	 */
	public MagicCardFilter getFilter() {
		return this.filter;
	}

	public IFilteredCardStore<ICard> getFilteredStore() {
		return this.fstore;
	}

	/**
	 * @return
	 */
	public ISelectionProvider getSelectionProvider() {
		return getViewer();
	}

	public Shell getShell() {
		return getControl().getShell();
	}

	public abstract ColumnViewer getViewer();

	/**
	 * @param menuMgr
	 */
	public void hookContextMenu(MenuManager menuMgr) {
		Menu menu = menuMgr.createContextMenu(getViewer().getControl());
		getViewer().getControl().setMenu(menu);
	}

	/**
	 * @param doubleClickListener
	 */
	public void hookDoubleClickListener(IDoubleClickListener doubleClickListener) {
		getViewer().addDoubleClickListener(doubleClickListener);
	}

	public void hookDragAndDrop() {
		this.getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer()));
		getViewer().addDropSupport(ops, transfers, new MagicCardDropAdapter(getViewer()));
	}

	public void setFilter(MagicCardFilter filter) {
		this.filter = filter;
	}

	public void setFilteredCardStore(IFilteredCardStore<ICard> store) {
		this.fstore = store;
	}

	public void setSortAction(IColumnSortAction sortAction2) {
		this.sortAction = sortAction2;
	}

	protected void sortColumn(final int coln) {
		if (sortAction != null)
			sortAction.sort(coln);
	}

	public void updateColumns(String preferenceValue) {
		// override to implement
	}

	/**
	 * @param indexCmc
	 */
	public void updateGroupBy(ICardField field) {
		ICardField oldIndex = this.filter.getGroupField();
		if (oldIndex == field)
			return;
		if (field != null)
			filter.setSortField(field, true);
		this.filter.setGroupField(field);
	}

	public abstract void updateSortColumn(int index);

	protected void updateTableHeader() {
		// to be overriden
	}

	public abstract void updateViewer();
}
