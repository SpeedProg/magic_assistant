package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;

public abstract class ColumnManager extends ColumnLabelProvider {
	protected int dataIndex;

	public ColumnManager(int dataIndex) {
		this.dataIndex = dataIndex;
	}

	public abstract String getColumnName();

	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			return ((CardGroup) element).getFieldByIndex(getDataIndex());
		}
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			try {
				return card.getByIndex(getDataIndex());
			} catch (ArrayIndexOutOfBoundsException e) {
				return "n/a";
			}
		}
		return "?";
	}

	public String getColumnTooltip() {
		if (getColumnName().equals(getColumnFullName()))
			return "";
		else
			return getColumnFullName();
	}

	public int getColumnWidth() {
		return 100;
	}

	public int getDataIndex() {
		return this.dataIndex;
	}

	public int getSortIndex() {
		return getDataIndex();
	}

	public String getColumnFullName() {
		return getColumnName();
	}

	/**
	 * @param viewer
	 * @return
	 */
	public EditingSupport getEditingSupport(ColumnViewer viewer) {
		return null;
	};
}
