package com.reflexit.magiccards.ui.views.printings;

import java.util.Collection;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.TreeViewerManager;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.LanguageColumn;
import com.reflexit.magiccards.ui.views.columns.SetColumn;

public class PrintingsManager extends TreeViewerManager implements IDisposable {
	private boolean groupped = false;

	protected PrintingsManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		super.createContents(parent);
		this.viewer.setComparator(null);
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
	protected ColumnCollection doGetColumnCollection(String viewId) {
		return new ColumnCollection() {
			@Override
			protected void createColumns() {
				columns.add(new GroupColumn());
				columns.add(new SetColumn(true));
				columns.add(new LanguageColumn());
			}
		};
	}

	@Override
	protected void updateTableHeader() {
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		hideColumn(0, !groupped, acolumns);
	}

	@Override
	public void flip(boolean hasGroups) {
		groupped = hasGroups;
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		hideColumn(0, !hasGroups, acolumns);
	}

	@Override
	public void updateColumns(String preferenceValue) {
		// ignore
	}

	@Override
	public String getColumnLayoutProperty() {
		// ignore
		return "";
	}
}
