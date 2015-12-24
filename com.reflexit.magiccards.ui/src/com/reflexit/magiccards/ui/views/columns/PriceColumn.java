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

/**
 * @author Alena
 *
 */
public class PriceColumn extends SellerPriceColumn {
	public PriceColumn() {
		super(MagicCardField.PRICE, "User Price");
	}

	@Override
	public String getColumnFullName() {
		return "User Price";
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return getGenericEditingSupport(viewer);
	}

	@Override
	protected void setElementValue(Object element, Object value) {
		float price;
		if (value instanceof String && ((String) value).length() == 0) {
			price = 0;
		} else {
			price = value == null ? 0 : Float.parseFloat(value.toString());
		}
		super.setElementValue(element, price);
	}

	@Override
	protected CellEditor getElementCellEditor(Composite viewerControl) {
		TextCellEditor editor = new TextCellEditor(viewerControl, SWT.NONE);
		((Text) editor.getControl()).setTextLimit(5);
		((Text) editor.getControl()).addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				// validation - mine was for an Integer (also allow 'enter'):
				e.doit = "0123456789.".indexOf(e.text) >= 0 || e.character == '\0';
			}
		});
		return editor;
	}
}