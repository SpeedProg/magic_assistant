package com.reflexit.magiccards.ui.views.columns;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class SetColumn extends GenColumn {
	private boolean showImage = false;

	public boolean isShowImage() {
		return showImage;
	}

	public void setShowImage(boolean showImage) {
		this.showImage = showImage;
	}

	public SetColumn() {
		super(MagicCardField.SET, "Set");
	}

	public SetColumn(boolean showImage) {
		this();
		this.showImage = showImage;
	}

	@Override
	public int getColumnWidth() {
		return 150;
	}

	@Override
	public Image getImage(Object element) {
		if (isShowImage()) {
			if (element instanceof IMagicCard) {
				IMagicCard card = (IMagicCard) element;
				return ImageCreator.getInstance().getSetImage(card);
			}
		}
		return super.getImage(element);
	}

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
				IMagicCardPhysical card = (IMagicCardPhysical) element;
				List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
				if (cards.size() <= 1)
					return null;
				String sets[] = new String[cards.size()];
				int i = 0;
				for (Iterator iterator = cards.iterator(); iterator.hasNext(); i++) {
					IMagicCard iMagicCard = (IMagicCard) iterator.next();
					sets[i] = iMagicCard.getSet();
				}
				CellEditor editor = new ComboBoxCellEditor((Composite) viewer.getControl(), sets, SWT.READ_ONLY);
				return editor;
			}

			@Override
			protected void initializeCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
				String value = (String) getValue(cell.getElement());
				cellEditor.setValue(indexOf(value, ((ComboBoxCellEditor) cellEditor).getItems()));
			}

			private int indexOf(String value, String[] items) {
				for (int i = 0; i < items.length; i++) {
					if (items[i].equals(value))
						return i;
				}
				return -1;
			}

			@Override
			protected void saveCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
				Object value = cellEditor.getValue();
				String set = ((ComboBoxCellEditor) cellEditor).getItems()[(Integer) value];
				setValue(cell.getElement(), set);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof MagicCardPhysical) {
					IMagicCardPhysical card = (IMagicCardPhysical) element;
					return card.getSet();
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					// set
					IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
					ICardStore<IMagicCard> cardStore = target.getCardStore();
					List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
					String set = (String) value;
					for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
						IMagicCard iMagicCard = (IMagicCard) iterator.next();
						if (iMagicCard.getSet().equals(set)) {
							card.setMagicCard((MagicCard) iMagicCard);
							break;
						}
					}
					DataManager.reconcile();
					// update
					cardStore.update(card);
					// viewer.update(element, null);
				}
			}
		};
	}
}
