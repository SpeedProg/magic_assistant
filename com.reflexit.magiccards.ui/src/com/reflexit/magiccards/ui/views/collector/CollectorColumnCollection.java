package com.reflexit.magiccards.ui.views.collector;

import com.reflexit.magiccards.ui.preferences.CollectorViewPreferencePage;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class CollectorColumnCollection extends MagicColumnCollection {
	public CollectorColumnCollection() {
		super(CollectorViewPreferencePage.class.getName());
	}

	@Override
	protected void createColumns() {
		super.createColumns();
		columns.add(new ProgressColumn());
	}
}