package com.reflexit.magiccards.ui.views.columns;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardModifiable;
import com.reflexit.magiccards.core.xml.StringCache;

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
		card.set(column.getDataField(), StringCache.intern((String) value));
		Set<ICardField> of = Collections.singleton(column.getDataField());
		DataManager.getInstance().update((IMagicCard) card, of);
		// getViewer().refresh(true);
	}
}