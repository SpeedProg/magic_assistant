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
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * @author Alena
 * 
 */
public class CommentColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public CommentColumn() {
		super(MagicCardField.COMMENT, "Comment");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof MagicCardPhysical) {
			IMagicCardPhysical m = (IMagicCardPhysical) element;
			String comm = m.getComment();
			if (comm == null)
				return "";
			return comm;
		} else {
			return super.getText(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.reflexit.magiccards.ui.views.columns.ColumnManager#getEditingSupport(org.eclipse.jface
	 * .viewers.ColumnViewer)
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
					IMagicCardPhysical card = (IMagicCardPhysical) element;
					String loc = card.getComment();
					if (loc == null)
						return "";
					return loc;
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					card.setComment((String) value);
					Set<MagicCardField> of = Collections.singleton(MagicCardField.COMMENT);
					DataManager.getInstance().update(card, of);
				}
			}
		};
	}
}