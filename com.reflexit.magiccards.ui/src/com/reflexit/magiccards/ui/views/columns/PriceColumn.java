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

import java.text.DecimalFormat;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

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
		super(MagicCardFieldPhysical.PRICE, "Price");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof MagicCardPhisical) {
			MagicCardPhisical m = (MagicCardPhisical) element;
			return "$" + decimalFormat.format(m.getPrice());
		} else {
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.columns.ColumnManager#getEditingSupport(org.eclipse.jface.viewers.TableViewer)
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
				if (element instanceof MagicCardPhisical) {
					MagicCardPhisical card = (MagicCardPhisical) element;
					float price = card.getPrice();
					return String.valueOf(price);
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhisical) {
					MagicCardPhisical card = (MagicCardPhisical) element;
					float price = value == null ? 0 : Float.parseFloat(value.toString());
					// save
					IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
					ICardStore<IMagicCard> cardStore = target.getCardStore();
					card.setPrice(price);
					cardStore.update(card);
				}
			}
		};
	}
}