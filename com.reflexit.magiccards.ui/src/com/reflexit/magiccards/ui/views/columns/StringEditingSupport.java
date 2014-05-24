package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.ICardModifiable;

public class StringEditingSupport extends EditingSupport {
	private AbstractColumn column;

	public StringEditingSupport(ColumnViewer viewer, AbstractColumn col) {
		super(viewer);
		this.column = col;
	}

	@Override
	protected boolean canEdit(Object element) {
		return element instanceof ICardModifiable;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		return new TextCellEditor((Composite) getViewer().getControl(), SWT.NONE);
	}

	@Override
	protected Object getValue(Object element) {
		return column.getText(element);
	}

	@Override
	protected void setValue(Object element, Object value) {
		ICardModifiable card = (ICardModifiable) element;
		card.set(column.getDataField(), value);
		getViewer().refresh(true);
	}
}