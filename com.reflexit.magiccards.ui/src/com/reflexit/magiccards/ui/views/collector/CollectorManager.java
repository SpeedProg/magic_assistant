package com.reflexit.magiccards.ui.views.collector;

import java.util.Collection;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.views.ViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.LanguageColumn;
import com.reflexit.magiccards.ui.views.columns.LocationColumn;

public class CollectorManager extends ViewerManager implements IDisposable {
	private TreeViewer viewer;
	private CollectorViewerComparator vcomp = new CollectorViewerComparator();
	private boolean groupped = true;

	protected CollectorManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new CollectorContentProvider());
		this.viewer.setUseHashlookup(true);
		this.viewer.setComparator(null);
		createDefaultColumns();
		hookDragAndDrop();
		return this.viewer.getControl();
	}

	@Override
	public void hookDragAndDrop() {
		this.getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer()));
	}

	public void setInput(Collection<Object> input) {
		this.viewer.setInput(input);
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	@Override
	public void dispose() {
		this.viewer.getControl().dispose();
		this.viewer = null;
	}

	@Override
	protected ColumnCollection doGetColumnCollection(String viewId) {
		return new ColumnCollection() {
			@Override
			protected void createColumns() {
				columns.add(new GroupColumn(MagicCardField.SET, "Set") {
					@Override
					public String getText(Object element) {
						if (element instanceof CardGroup) {
							return ((CardGroup) element).getName();
						} else if (element instanceof IMagicCard) {
							return ((IMagicCard) element).getName();
						}
						return null;
					}

					@Override
					public Image getImage(Object element) {
						if (element instanceof IMagicCard) {
							IMagicCard card = (IMagicCard) element;
							return ImageCreator.getInstance().getSetImage(card);
						}
						return null;
					}

					@Override
					public int getColumnWidth() {
						return 200;
					}
				});
				columns.add(new ProgressColumn());
				columns.add(new LocationColumn());
				columns.add(new LanguageColumn());
			}
		};
	}

	protected void createDefaultColumns() {
		getColumnsCollection().createColumnLabelProviders();
		for (int i = 0; i < getColumnsNumber(); i++) {
			AbstractColumn man = getColumn(i);
			TreeViewerColumn colv = new TreeViewerColumn(this.viewer, i);
			TreeColumn col = colv.getColumn();
			col.setText(man.getColumnName());
			col.setWidth(man.getColumnWidth());
			col.setToolTipText(man.getColumnTooltip());
			final int coln = i;
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sortColumn(coln);
				}
			});
			col.setMoveable(false);
			colv.setLabelProvider(man);
			if (man instanceof Listener) {
				this.viewer.getTree().addListener(SWT.PaintItem, (Listener) man);
			}
			colv.setEditingSupport(man.getEditingSupport(this.viewer));
		}
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		this.viewer.getTree().setHeaderVisible(true);
	}

	public void setSortColumn(int index, int direction) {
		boolean sort = index >= 0;
		TreeColumn column = sort ? this.viewer.getTree().getColumn(index) : null;
		this.viewer.getTree().setSortColumn(column);
		if (sort) {
			int sortDirection = getSortDirection();
			if (sortDirection != SWT.DOWN)
				sortDirection = SWT.DOWN;
			else
				sortDirection = SWT.UP;
			this.viewer.getTree().setSortDirection(sortDirection);
			AbstractColumn man = (AbstractColumn) this.viewer.getLabelProvider(index);
			if (man instanceof GroupColumn) {
				((GroupColumn) man).setGroupField(man.getDataField());
			}
			vcomp.setOrder(man.getSortField(), sortDirection == SWT.UP);
			this.viewer.setComparator(vcomp);
		} else {
			this.viewer.setComparator(null);
		}
	}

	@Override
	public int getSortDirection() {
		return this.viewer.getTree().getSortDirection();
	}

	public void updateViewer(Object input) {
		if (this.viewer.getControl().isDisposed())
			return;
		updateTableHeader();
		updateGrid();
		this.viewer.setInput(input);
	}

	@Override
	protected void updateTableHeader() {
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		hideColumn(0, !groupped, acolumns);
	}

	@Override
	public void flip(boolean hasGroups) {
		groupped = hasGroups;
		if (viewer != null) {
			TreeColumn[] acolumns = this.viewer.getTree().getColumns();
			hideColumn(0, !hasGroups, acolumns);
		}
	}

	private void hideColumn(int i, boolean hide, TreeColumn[] acolumns) {
		TreeColumn column = acolumns[i];
		if (hide)
			column.setWidth(0);
		else if (column.getWidth() <= 0) {
			int def = getColumn(i).getColumnWidth();
			column.setWidth(def);
		}
	}

	public void setLinesVisible(boolean grid) {
		this.viewer.getTree().setLinesVisible(grid);
	}

	public void updateColumns(String preferenceValue) {
		// ignore
	}
}
