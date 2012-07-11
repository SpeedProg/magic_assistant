package com.reflexit.magiccards.ui.views.collector;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.ui.preferences.CollectorViewPreferencePage;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
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

	@Override
	protected GroupColumn createGroupColumn() {
		return new GroupColumn() {
			@Override
			public String getText(Object element) {
				if (element instanceof CardGroup) {
					return ((CardGroup) element).getName();
				}
				return super.getText(element);
			}
		};
	}
}