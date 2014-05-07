package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * @author Alena
 * 
 */
public class StringEditorColumn extends GenColumn {
	public StringEditorColumn(ICardField field, String columnName) {
		super(field, columnName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.columns.ColumnManager#getEditingSupport
	 * (org.eclipse.jface.viewers.ColumnViewer)
	 */
	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof MagicCardPhysical)
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
				if (element instanceof MagicCardPhysical) {
					String loc = getText(element);
					return loc;
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					// move
					card.set(getDataField(), value);
					DataManager.update(card);
				}
			}
		};
	}
}