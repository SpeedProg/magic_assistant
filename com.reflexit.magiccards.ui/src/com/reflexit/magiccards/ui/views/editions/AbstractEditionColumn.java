package com.reflexit.magiccards.ui.views.editions;

import java.io.FileNotFoundException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class AbstractEditionColumn extends ColumnLabelProvider {
	protected int columnIndex = -1;
	private String name;
	private EditionField field;

	public AbstractEditionColumn(String name, EditionField field) {
		this.name = name;
		this.field = field;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public String getColumnName() {
		return name;
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

	public String getColumnFullName() {
		return getColumnName();
	}

	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return createStringEditingSupport(viewer);
	}

	public EditionField getSortField() {
		return field;
	}

	public EditingSupport createStringEditingSupport(final ColumnViewer viewer) {
		return new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Edition)
					return true;
				else
					return false;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				TextCellEditor editor = new TextCellEditor((Composite) viewer.getControl(), SWT.NONE);
				return editor;
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Edition) {
					return getTextForEdit((Edition) element);
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Edition) {
					Edition edition = (Edition) element;
					String string = (String) value;
					try {
						setText(edition, string);
					} catch (Exception e) {
						MagicUIActivator.log(e);
						MessageDialog.openError(getViewer().getControl().getShell(), "Error",
								e.getMessage());
						return;
					}
					getViewer().update(element, null);
					try {
						Editions.getInstance().save();
					} catch (FileNotFoundException e) {
						MagicUIActivator.log(e);
					}
				}
			}
		};
	}

	public void setText(Edition edition, String string) {
		throw new UnsupportedOperationException();
	}

	public String getTextForEdit(Edition element) {
		return getText(element);
	}
}
