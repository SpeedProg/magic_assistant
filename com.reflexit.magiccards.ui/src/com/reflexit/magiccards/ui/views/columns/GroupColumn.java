package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class GroupColumn extends GenColumn {
	public static final String COL_NAME = "Name";
	private ICardField groupField;
	protected final boolean showCount;
	protected final boolean showImage;
	protected final boolean canEdit;

	public GroupColumn() {
		this(true, true, false);
	}

	public GroupColumn(boolean showCount, boolean showSetImage, boolean canEdit) {
		super(MagicCardField.NAME, COL_NAME);
		this.showCount = showCount;
		this.showImage = showSetImage;
		this.canEdit = canEdit;
	}

	@Override
	public Image getImage(Object element) {
		if (showImage) {
			if (element instanceof ICardGroup) {
				if (((CardGroup) element).getFieldIndex() == MagicCardField.NAME) {
					return ImageCreator.getInstance().getSetImage(((CardGroup) element).getFirstCard());
				}
			} else if (element instanceof IMagicCard) {
				IMagicCard card = (IMagicCard) element;
				return ImageCreator.getInstance().getSetImage(card);
			}
		}
		return null;
	}

	@Override
	public int getColumnWidth() {
		return 200;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ICardGroup) {
			if (!showCount) {
				return ((CardGroup) element).getName();
			} else {
				return ((CardGroup) element).getName() + " (" + ((CardGroup) element).getCount() + ")";
			}
		} else if (element instanceof IMagicCard) {
			return ((IMagicCard) element).getName();
		}
		return null;
	}

	public void setGroupField(ICardField field) {
		groupField = field;
	}

	public ICardField getGroupField() {
		return groupField;
	}

	@Override
	public ICardField getSortField() {
		return groupField == null ? dataIndex : groupField;
	}

	@Override
	public ICardField getDataField() {
		return super.getDataField();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(true); // always visible
	}

	@Override
	public void setUserWidth(int width) {
		if (width == 0)
			super.setUserWidth(getColumnWidth());
		else
			super.setUserWidth(width);
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		if (!canEdit)
			return null;
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
					return ((MagicCardPhysical) element).getName();
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					MagicCard base = (MagicCard) card.getBase().clone();
					base.setName((String) value);
					base.setCardId(0);
					card.setMagicCard(base);
					ImportUtils.updateCardReference(card);
					Object input = getViewer().getInput();
					if (input instanceof IFilteredCardStore) {
						IFilteredCardStore target = (IFilteredCardStore) input;
						ICardStore<IMagicCard> cardStore = target.getCardStore();
						DataManager.reconcile();
						// update
						cardStore.update(card);
					} else {
						// update
						viewer.refresh(true);
					}
				}
			}
		};
	}
}
