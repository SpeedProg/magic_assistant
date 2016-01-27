package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;

public abstract class AbstractColumn extends ColumnLabelProvider {
	protected final ICardField dataIndex;
	protected int columnIndex = -1;
	protected int userwidth;
	private boolean visible = true;

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public AbstractColumn(ICardField dataIndex) {
		this.dataIndex = dataIndex;
		this.userwidth = getColumnWidth();
	}

	public abstract String getColumnName();

	@Override
	public String getText(Object element) {
		if (!visible)
			return "";
		return getActualText(element);
	}

	protected String getActualText(Object element) {
		if (element instanceof ICardGroup) {
			ICardGroup group = (ICardGroup) element;
			if (group.isTransient() && group != group.getFirstCard()) {
				return getActualText(group.getFirstCard());
			}
		}
		if (element instanceof ICard) {
			ICard card = (ICard) element;
			try {
				ICardField field = getDataField();
				Object value = card.get(field);
				if (value == null)
					return "";
				return value.toString();
			} catch (ArrayIndexOutOfBoundsException e) {
				return "n/a";
			}
		}
		return "";
	}

	public String getColumnTooltip() {
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

	public int getUserWidth() {
		return userwidth;
	}

	public void setUserWidth(int width) {
		this.userwidth = width;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void updateOnEdit(final ColumnViewer viewer, IMagicCard card) {
		Object input = viewer.getInput();
		if (input instanceof IFilteredCardStore) {
			// update
			((IFilteredCardStore) input).getCardStore().reindex();
			DataManager.getInstance().update(card, null);
		} else {
			// update
			viewer.refresh(true);
		}
	}

	@Override
	public Color getBackground(Object element) {
		// if (element instanceof ICardGroup) {
		// return
		// Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
		// }
		boolean own = false;
		if (element instanceof IMagicCardPhysical) {
			own = ((IMagicCardPhysical) element).isOwn();
		}
		return MagicUIActivator.getDefault().getBgColor(own);
	}

	@Override
	public String toString() {
		return getColumnName();
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	protected void setElementValue(Object element, Object value) {
		DataManager.getInstance().setField((MagicCardPhysical) element, getDataField(), value);
	}

	protected boolean canEditElement(Object element) {
		return element instanceof MagicCardPhysical;
	}

	protected String getElementValue(Object element) {
		return getText(element);
	}

	public EditingSupport getGenericEditingSupport(final ColumnViewer viewer) {
		return new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (canEditElement(element))
					return true;
				else
					return false;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				Composite viewerControl = (Composite) viewer.getControl();
				return getElementCellEditor(viewerControl);
			}

			@Override
			protected Object getValue(Object element) {
				if (canEdit(element)) {
					return getElementValue(element);
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (canEdit(element)) {
					setElementValue(element, value);
				}
			}
		};
	}

	protected CellEditor getElementCellEditor(Composite viewerControl) {
		TextCellEditor editor = new TextCellEditor(viewerControl, SWT.NONE);
		return editor;
	}
}
