package com.reflexit.magiccards.ui.views.columns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardModifiable;

public class CreationDateColumn extends GenColumn {
	public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd MMM yyyy");

	public CreationDateColumn() {
		super(MagicCardField.DATE, "Date");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			Object object = ((IMagicCard) element).get(dataIndex);
			if (object instanceof Date) {
				return DATE_FORMATTER.format(object);
			}
		}
		return super.getText(element);
	}

	@Override
	public String getColumnTooltip() {
		return "Date when card originally added to collection";
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return new StringEditingSupport(viewer, this) {
			@Override
			protected void setValue(Object element, Object value) {
				ICardModifiable card = (ICardModifiable) element;
				if (value instanceof String) {
					try {
						Date date = DATE_FORMATTER.parse((String) value);
						card.set(getDataField(), date);
						getViewer().refresh(true);
					} catch (ParseException e) {
						MessageDialog.openError(viewer.getControl().getShell(), "Error",
								"Cannot parse date in format '" + DATE_FORMATTER.toPattern() + "'");
					}
				}
			}
		};
	}
}
