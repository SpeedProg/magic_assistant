package com.reflexit.magiccards.ui.views;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.model.RootTreeViewerContentProvider;

public class SingleColumnTreeViewer extends ExtendedTreeViewer {
	private GroupColumn labelProvider = new GroupColumn(true, true, false) {
		@Override
		public String getColumnName() {
			ICardField groupField = getGroupField();
			if (groupField == null)
				groupField = getDataField();
			return groupField.getLabel();
		}
	};
	private ColumnCollection columns = new ColumnCollection() {
		@Override
		protected void createColumns(List<AbstractColumn> columns) {
			columns.add(labelProvider);
		}
	};

	@Override
	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return columns;
	};

	public SingleColumnTreeViewer(Composite parent) {
		super(parent);
		setColumnCollection(doGetColumnCollection(""));
		setContentProvider(new RootTreeViewerContentProvider());
		setAutoExpandLevel(2);
		getTree().setHeaderVisible(false);
		// setLabelProvider(labelProvider);
	}

	@Override
	protected void createFillerColumn() {
		// not
	}

	@Override
	protected void createLabelProviders() {
		super.createLabelProviders();
	}
}
