package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.CardGroup;

public class NameColumn extends GenColumn {
	public NameColumn(int dataIndex) {
		super(dataIndex, "Name");
	}

	@Override
	public int getColumnWidth() {
		return 200;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.columns.ColumnManager#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			return ((CardGroup) element).getName();
		}
		return super.getText(element);
	}
}
