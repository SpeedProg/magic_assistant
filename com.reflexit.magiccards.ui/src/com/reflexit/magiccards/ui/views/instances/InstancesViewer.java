package com.reflexit.magiccards.ui.views.instances;

import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.ExtendedTreeViewer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.CommentColumn;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GenColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.LanguageColumn;
import com.reflexit.magiccards.ui.views.columns.LocationColumn;
import com.reflexit.magiccards.ui.views.columns.OwnershipColumn;
import com.reflexit.magiccards.ui.views.columns.PriceColumn;
import com.reflexit.magiccards.ui.views.columns.SetColumn;
import com.reflexit.magiccards.ui.views.columns.StringEditorColumn;

public class InstancesViewer extends ExtendedTreeViewer implements IDisposable {
	private boolean groupped = false;

	protected InstancesViewer(String id, Composite parent) {
		super(parent, id);
		setComparator(null);
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
				columns.add(new GroupColumn(true, false, false));
				columns.add(new SetColumn(true));
				columns.add(new CountColumn());
				columns.add(new OwnershipColumn());
				columns.add(new LocationColumn());
				columns.add(new LanguageColumn());
				columns.add(new StringEditorColumn(MagicCardField.SPECIAL, "Special"));
				columns.add(new CommentColumn());
				columns.add(new PriceColumn());
				if (MagicUIActivator.TRACE_EXPORT) {
					columns.add(new GenColumn(MagicCardField.HASHCODE, "HashCode"));
				}
			}
		};
	}

	protected void updateTableHeader() {
		showColumn(0, groupped);
	}

	public void setGrouppingEnabled(boolean hasGroups) {
		groupped = hasGroups;
		if (getViewer() == null)
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
