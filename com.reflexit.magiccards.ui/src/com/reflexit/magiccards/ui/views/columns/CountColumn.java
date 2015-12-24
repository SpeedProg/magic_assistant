package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;

/**
 * @author Alena
 * 
 */
public class CountColumn extends GenColumn {
	public CountColumn() {
		super(MagicCardField.COUNT, "Count");
	}

	public CountColumn(ICardField field, String name) {
		super(field, name);
	}

	@Override
	public String getText(Object element) {
		return super.getText(element);
	}

	@Override
	public int getColumnWidth() {
		return 45;
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return getGenericEditingSupport(viewer);
	}

	@Override
	protected void setElementValue(Object element, Object value) {
		if (value instanceof String) {
			value = Integer.valueOf((String) value);
		}
		super.setElementValue(element, value);
	}

	@Override
	protected CellEditor getElementCellEditor(Composite viewerControl) {
		TextCellEditor editor = new TextCellEditor(viewerControl, SWT.NONE);
		Text textControl = (Text) editor.getControl();
		textControl.setTextLimit(5);
		textControl.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				// validation - mine was for an Integer (also allows 'enter'):
				e.doit = "0123456789".indexOf(e.text) >= 0 || e.character == '\0';
			}
		});
		return editor;
	}
}