package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;

public class GroupColumn extends GenColumn {
	public static final String COL_NAME = "Group";
	private ICardField groupField;

	public GroupColumn() {
		super(null, COL_NAME);
	}

	@Override
	public int getColumnWidth() {
		return 100;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			return ((CardGroup) element).getName() + " (" + ((CardGroup) element).getCount() + ")";
		}
		return null;
	}

	public void setGroupField(ICardField field) {
		groupField = field;
	}

	public ICardField getGroupField() {
		return groupField;
	}

	@Override
	public ICardField getDataField() {
		return groupField;
	}
}
