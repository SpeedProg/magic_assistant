package com.reflexit.magiccards.ui.views.editions;

import java.io.FileNotFoundException;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.ui.MagicUIActivator;

final class FormatColumn extends AbstractEditionColumn {
	FormatColumn() {
		super("Format", EditionField.FORMAT);
	}

	@Override
	public String getText(Object element) {
		Editions.Edition ed = (Edition) element;
		return ed.getFormatString();
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
					edition.setFormats(string);
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
}