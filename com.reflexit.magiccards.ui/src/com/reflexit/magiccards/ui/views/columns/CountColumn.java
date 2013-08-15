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

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * @author Alena
 * 
 */
public class CountColumn extends GenColumn {
	/**
	 */
	public CountColumn() {
		super(MagicCardFieldPhysical.COUNT, "Count");
	}

	public CountColumn(ICardField field, String name) {
		super(field, name);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ICardCountable) {
			ICardCountable m = (ICardCountable) element;
			return String.valueOf(m.getCount());
		} else {
			return "";
		}
	}

	@Override
	public int getColumnWidth() {
		return 45;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.columns.ColumnManager#getEditingSupport
	 * (org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (CountColumn.this.canEdit(element) && (viewer.getInput() instanceof IFilteredCardStore))
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
						// validation - mine was for an Integer (also allow
						// 'enter'):
						e.doit = "0123456789".indexOf(e.text) >= 0 || e.character == '\0';
					}
				});
				return editor;
			}

			@Override
			protected Object getValue(Object element) {
				if (CountColumn.this.canEdit(element)) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					int count = card.getCount();
					return String.valueOf(count);
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (viewer.getInput() instanceof IFilteredCardStore) {
					if (CountColumn.this.canEdit(element)) {
						MagicCardPhysical card = (MagicCardPhysical) element;
						int oldCount = card.getCount();
						int count = value == null ? 0 : Integer.parseInt(value.toString());
						if (oldCount == count)
							return;
						// MagicCardPhysical add = new MagicCardPhysical(card, card.getLocation());
						// add.setCount(count);
						card.setCount(count);
						// viewer.update(element, null);
						// save
						DataManager.update(card);
						// IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
						// ICardStore<IMagicCard> cardStore = target.getCardStore();
						// cardStore.remove(card);
						// cardStore.add(add);
						// cardStore.update(add);
					}
				}
			}
		};
	}

	protected boolean canEdit(Object element) {
		return element instanceof MagicCardPhysical;
	}
}