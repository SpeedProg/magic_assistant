package com.reflexit.magiccards.ui.views.columns;

import java.util.Collections;

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
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

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
		return new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (canEditElement(element) && viewer.getInput() instanceof IFilteredCardStore)
					return true;
				else
					return false;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				Composite viewerControl = (Composite) viewer.getControl();
				return getElementCellEditor(viewerControl);
			}

			@Override
			protected Object getValue(Object element) {
				if (canEdit(element)) {
					return getElementValue(element);
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (canEdit(element)) {
					setElementValue(element, value);
				}
			}
		};
	}

	protected void setElementValue(Object element, Object value) {
		MagicCardPhysical card = (MagicCardPhysical) element;
		int oldCount = card.getCount();
		int count = value == null ? 0 : Integer.parseInt(value.toString());
		if (oldCount == count)
			return;
		card.setCount(count);
		// save
		DataManager.getInstance().update(card, Collections.singleton(getDataField()));
	}

	protected boolean canEditElement(Object element) {
		return element instanceof MagicCardPhysical;
	}

	protected String getElementValue(Object element) {
		return getText(element);
	}

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