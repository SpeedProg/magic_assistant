package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * @author Alena
 *
 */
public class CommentColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public CommentColumn() {
		super(MagicCardFieldPhysical.COMMENT, "Comment");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof MagicCardPhisical) {
			MagicCardPhisical m = (MagicCardPhisical) element;
			String comm = m.getComment();
			return comm;
		} else {
			return super.getText(element);
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.columns.ColumnManager#getEditingSupport(org.eclipse.jface.viewers.ColumnViewer)
	 */
	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof MagicCardPhisical)
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
				if (element instanceof MagicCardPhisical) {
					MagicCardPhisical card = (MagicCardPhisical) element;
					String loc = card.getComment();
					return loc;
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhisical) {
					MagicCardPhisical card = (MagicCardPhisical) element;
					// move
					IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
					ICardStore<IMagicCard> cardStore = target.getCardStore();
					card.setComment((String) value);
					cardStore.update(card);
					//					// update
					//					viewer.update(element, null);
				}
			}
		};
	}
}