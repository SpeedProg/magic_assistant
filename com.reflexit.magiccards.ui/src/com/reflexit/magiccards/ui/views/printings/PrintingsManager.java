package com.reflexit.magiccards.ui.views.printings;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.TreeViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
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
		hookDragAndDrop(getViewer());
		return this.viewer.getControl();
	}

	@Override
	public void hookDragAndDrop(StructuredViewer viewer) {
		this.getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(ops, new Transfer[] { MagicCardTransfer.getInstance(), TextTransfer.getInstance(),
				PluginTransfer.getInstance() }, new MagicCardDragListener(viewer));
	}

	public void setInput(Collection<Object> input) {
		this.viewer.setInput(input);
	}

	@Override
	protected ColumnCollection doGetColumnCollection(String viewId) {
		return new ColumnCollection() {
			@Override
			protected void createColumns(List<AbstractColumn> columns) {
				columns.add(new GroupColumn());
				columns.add(new SetColumn(true));
				columns.add(new LanguageColumn());
			}
		};
	}

	@Override
	protected void updateTableHeader() {
		showColumn(0, groupped);
	}

	@Override
	public void setGrouppingEnabled(boolean hasGroups) {
		groupped = hasGroups;
		if (viewer == null)
			return;
		updateTableHeader();
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
