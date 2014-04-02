package com.reflexit.magiccards.ui.views.columns;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * @author Alena
 * 
 */
public class PriceColumn extends GenColumn {
	DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	/**
	 * @param columnName
	 */
	public PriceColumn() {
		super(MagicCardField.PRICE, "Price");
	}

	@Override
	public String getText(Object element) {
		String text = super.getText(element);
		if (text.length() == 0)
			return text;
		if (text.equals("0.0"))
			return "";
		return "$" + decimalFormat.format(Float.parseFloat(text));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.reflexit.magiccards.ui.views.columns.ColumnManager#getEditingSupport(org.eclipse.jface
	 * .viewers.TableViewer)
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
			protected CellEditor getCellEditor(Object element) {
				TextCellEditor editor = new TextCellEditor((Composite) viewer.getControl(), SWT.NONE);
				((Text) editor.getControl()).setTextLimit(5);
				((Text) editor.getControl()).addVerifyListener(new VerifyListener() {
					public void verifyText(VerifyEvent e) {
						// validation - mine was for an Integer (also allow 'enter'):
						e.doit = "0123456789.".indexOf(e.text) >= 0 || e.character == '\0';
					}
				});
				return editor;
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					float price = card.getPrice();
					return String.valueOf(price);
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					float price;
					if (value instanceof String && ((String) value).length() == 0) {
						price = 0;
					} else {
						price = value == null ? 0 : Float.parseFloat(value.toString());
					}
					// save
					card.setPrice(price);
					DataManager.update(card);
				}
			}
		};
	}

	@Override
	public int getColumnWidth() {
		return 50;
	}
}