package com.reflexit.magiccards.ui.widgets;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public abstract class ComboStringEditingSupport extends EditingSupport {
	public ComboStringEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		String[] sets = getItems(element);
		if (sets == null)
			return null;
		CellEditor editor = new ComboBoxCellEditor((Composite) getViewer().getControl(), sets, SWT.READ_ONLY);
		return editor;
	}

	public abstract String[] getItems(final Object element);

	@Override
	protected void initializeCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		String value = (String) getValue(cell.getElement());
		cellEditor.setValue(indexOf(value, ((ComboBoxCellEditor) cellEditor).getItems()));
	}

	private int indexOf(String value, String[] items) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(value))
				return i;
		}
		return -1;
	}

	@Override
	protected void saveCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		Object value = cellEditor.getValue();
		try {
			String set = ((ComboBoxCellEditor) cellEditor).getItems()[(Integer) value];
			setValue(cell.getElement(), set);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
