package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;

public abstract class AbstractColumn extends ColumnLabelProvider {
	protected final ICardField dataIndex;
	protected int columnIndex = -1;
	protected int orderIndex = -1;
	protected int width;
	protected boolean hidden = false;

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
		this.orderIndex = columnIndex;
	}

	public AbstractColumn(ICardField dataIndex) {
		this.dataIndex = dataIndex;
		this.width = getColumnWidth();
	}

	public abstract String getColumnName();

	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			ICardField field = getDataField();
			return ((CardGroup) element).getLabelByField(field);
		}
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			try {
				ICardField field = getDataField();
				Object value = card.getObjectByField(field);
				if (value == null)
					return "";
				return value.toString();
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

	public ICardField getDataField() {
		return this.dataIndex;
	}

	public ICardField getSortField() {
		return getDataField();
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
	}

	public int getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
}
