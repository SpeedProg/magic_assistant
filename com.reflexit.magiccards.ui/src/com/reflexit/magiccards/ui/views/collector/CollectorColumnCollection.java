package com.reflexit.magiccards.ui.views.collector;

import java.util.List;

import com.reflexit.magiccards.ui.preferences.CollectorViewPreferencePage;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class CollectorColumnCollection extends MagicColumnCollection {
	public CollectorColumnCollection() {
		super(CollectorViewPreferencePage.class.getName());
	}

	@Override
	protected void createColumns(List<AbstractColumn> columns) {
		super.createColumns(columns);
		columns.add(new ProgressColumn());
		columns.add(new Progress4Column());
	}

	@Override
	protected GroupColumn createGroupColumn() {
		return new GroupColumn(false, true, false);
	}
}