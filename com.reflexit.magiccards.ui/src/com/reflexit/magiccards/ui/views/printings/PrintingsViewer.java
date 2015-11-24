package com.reflexit.magiccards.ui.views.printings;

import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.ExtendedTreeViewer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.LanguageColumn;
import com.reflexit.magiccards.ui.views.columns.SetColumn;

public class PrintingsViewer extends ExtendedTreeViewer implements IDisposable {
	private boolean groupped = false;

	protected PrintingsViewer(String id, Composite parent) {
		super(parent, id);
		setComparator(null);
		hookDragAndDrop(this);
	}


	@Override
	public void hookDragAndDrop(StructuredViewer viewer) {
		this.getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(ops, new Transfer[] { MagicCardTransfer.getInstance(), TextTransfer.getInstance(),
				PluginTransfer.getInstance() }, new MagicCardDragListener(viewer));
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


	protected void updateTableHeader() {
		showColumn(0, groupped);
	}

	public void setGrouppingEnabled(boolean hasGroups) {
		groupped = hasGroups;
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
