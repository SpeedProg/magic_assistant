package com.reflexit.magiccards.ui.views.editions;

import java.io.FileNotFoundException;
import java.text.ParseException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.ui.MagicUIActivator;

final class DateColumn extends AbstractEditionColumn {
	public DateColumn() {
		super("Date", EditionField.DATE);
	}

	@Override
	public String getText(Object element) {
		Editions.Edition ed = (Edition) element;
		if (ed.getReleaseDate() == null)
			return "?";
		return EditionsComposite.formatter.format(ed.getReleaseDate());
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
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
					String loc = getText(element);
					return loc;
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Edition) {
					Edition edition = (Edition) element;
					String string = (String) value;
					try {
						edition.setReleaseDate(string);
						getViewer().update(element, null);
						Editions.getInstance().save();
					} catch (ParseException e) {
						MessageDialog.openError(getViewer().getControl().getShell(), "Error", "Enter day in format like: December 2010");
					} catch (FileNotFoundException e) {
						// cannot save editions
						MagicUIActivator.log(e);
					}
				}
			}
		};
	}
}