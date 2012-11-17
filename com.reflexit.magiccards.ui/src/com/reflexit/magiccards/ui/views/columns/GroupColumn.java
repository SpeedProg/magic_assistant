package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class GroupColumn extends GenColumn {
	public static final String COL_NAME = "Name";
	private ICardField groupField;
	protected boolean showCount = true;
	private boolean showImage = true;

	public GroupColumn() {
		super(MagicCardField.NAME, COL_NAME);
	}

	public GroupColumn(boolean showCount, boolean showSetImage) {
		this();
		this.showCount = showCount;
		this.showImage = showSetImage;
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
}
