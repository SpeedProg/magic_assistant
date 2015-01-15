package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

import com.reflexit.magiccards.core.model.abs.ICardField;

/**
 * @author Alena
 * 
 */
public class StringEditorColumn extends GenColumn {
	public StringEditorColumn(ICardField field, String columnName) {
		super(field, columnName);
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return new StringEditingSupport(viewer, this);
	}
}