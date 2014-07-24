package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class GroupColumn extends GenColumn implements Listener {
	public static final String COL_NAME = "Name";
	private ICardField groupField;
	protected final boolean showCount;
	protected boolean showImage;
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

	public void setShowImage(boolean show) {
		showImage = show;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	public Image getActualImage(Object element) {
		if (showImage) {
			if (element instanceof ICardGroup) {
				CardGroup cardGroup = (CardGroup) element;
				String set = cardGroup.getSet();
				if (set != null && set.length() > 0 && !set.equals("*")) {
					return ImageCreator.getInstance().getSetImage(cardGroup.getFirstCard());
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
				return ((CardGroup) element).getName() + " (" + getCount(element) + ")";
			}
		} else if (element instanceof IMagicCard) {
			return ((IMagicCard) element).getName();
		}
		return null;
	}

	protected int getCount(Object element) {
		return ((ICardCountable) element).getCount();
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
					card.setError(null);
					ImportUtils.updateCardReference(card);
					Object input = getViewer().getInput();
					if (input instanceof IFilteredCardStore) {
						DataManager.update(card);
						// update
					} else {
						// update
						viewer.refresh(true);
					}
				}
			}
		};
	}

	@Override
	protected void handleEraseEvent(Event event) {
		event.detail &= ~SWT.FOREGROUND;
	}

	@Override
	public void handlePaintEvent(Event event) {
		if (event.index == this.columnIndex) { // our column
			Item item = (Item) event.item;
			Object row = item.getData();
			int x = event.x;
			int y = event.y;
			Rectangle bounds = getBounds(event);
			int w = bounds.width;
			int h = bounds.height;
			int leftMargin = 0;
			Image image = getActualImage(row);
			if (image != null) {
				leftMargin = 32;
				event.gc.drawImage(image, x, y + 1);
			}
			String text = getText(row);
			if (text != null) {
				event.gc.setClipping(x, y, w - 3, h);
				event.gc.drawText(text, x + 3 + leftMargin, y + 1, true);
			}
		}
	}
}
