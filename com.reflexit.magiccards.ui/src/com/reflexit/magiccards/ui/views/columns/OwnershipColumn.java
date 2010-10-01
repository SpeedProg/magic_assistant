package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * @author Alena
 *
 */
public class OwnershipColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public OwnershipColumn() {
		super(MagicCardFieldPhysical.OWNERSHIP, "O");
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof MagicCardPhisical) {
			MagicCardPhisical m = (MagicCardPhisical) element;
			if (m.isOwn())
				return MagicUIActivator.getDefault().getImage("icons/obj16/ilib16.png");
			else
				return MagicUIActivator.getDefault().getImage("icons/obj16/monitor.gif");
		}
		return null;
	}

	@Override
	public int getColumnWidth() {
		return 20;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof MagicCardPhisical) {
			MagicCardPhisical m = (MagicCardPhisical) element;
			if (m.isOwn())
				return "own";
			else
				return "virtual";
		}
		return null;
	}

	@Override
	public String getColumnFullName() {
		return "Ownership";
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
				final Integer pos = (Integer) getValue(element);
				CellEditor editor = new ComboBoxCellEditor((Composite) viewer.getControl(), new String[] {
				        "own",
				        "virtual" }, SWT.READ_ONLY) {
				};
				editor.setValue(pos);
				return editor;
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof MagicCardPhisical) {
					MagicCardPhisical card = (MagicCardPhisical) element;
					Boolean ow = card.isOwn();
					return ow ? 0 : 1;
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhisical) {
					MagicCardPhisical card = (MagicCardPhisical) element;
					// set
					IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
					ICardStore<IMagicCard> cardStore = target.getCardStore();
					String string = value.toString();
					if (string.equals("0")) {
						card.setOwn(true);
					} else
						card.setOwn(false);
					// update
					cardStore.update(card);
					//viewer.update(element, null);
				}
			}
		};
	}
}